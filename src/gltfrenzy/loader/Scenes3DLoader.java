package gltfrenzy.loader;

import arc.assets.*;
import arc.assets.loaders.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.Mesh;
import arc.struct.*;
import arc.util.*;
import gltfrenzy.data.*;
import gltfrenzy.loader.Scenes3DLoader.*;
import gltfrenzy.loader.Scenes3DLoader.MeshContainerQueue.*;
import gltfrenzy.loader.Scenes3DReader.*;
import gltfrenzy.model.*;
import gltfrenzy.model.MeshSet.*;

import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * Asynchronous asset loader implementation to load {@link Scenes3D} assets from either {@code .gltf} or {@code .glb} files.
 * @author GlennFolker
 */
public class Scenes3DLoader extends AsynchronousAssetLoader<Scenes3D, Scenes3DParameter>{
    protected final Scenes3DReader reader;
    protected Scenes3D asset;
    protected MeshContainerQueue[] meshes;

    public Scenes3DLoader(FileHandleResolver resolver, Scenes3DReader reader){
        super(resolver);
        this.reader = reader;
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, Fi file, Scenes3DParameter parameter){
        if(parameter == null) parameter = new Scenes3DParameter();
        asset = parameter.asset != null ? parameter.asset : new Scenes3D();

        Scenes3DData data;
        try{
            data = reader.read(file);
        }catch(IOException e){
            throw new RuntimeException(e);
        }

        // TODO Handle URI buffers.
        var spec = data.spec;
        var buffers = data.buffers;
        var bufferViews = new ByteBuffer[spec.bufferViews.length];
        for(int i = 0; i < bufferViews.length; i++){
            var view = spec.bufferViews[i];

            var buffer = buffers[view.buffer];
            buffer.limit(view.byteOffset + view.byteLength);
            buffer.position(view.byteOffset);

            bufferViews[i] = buffer.slice();
            bufferViews[i].order(ByteOrder.LITTLE_ENDIAN);
            buffer.clear();
        }

        // TODO Handle morph targets.
        meshes = new MeshContainerQueue[spec.meshes.length];
        for(int i = 0; i < meshes.length; i++){
            var mesh = spec.meshes[i];
            var cont = meshes[i] = new MeshContainerQueue();
            cont.name = mesh.name == null ? "" : mesh.name;

            cont.containers = new MeshQueue[mesh.primitives.length];
            for(int m = 0; m < mesh.primitives.length; m++){
                var primitives = mesh.primitives[m];
                var attrs = primitives.attributes;

                int verticesLen = 0, verticesCount = -1;
                Seq<VertexAttribute> attributes = new Seq<>(true, attrs.size, VertexAttribute.class);

                for(int j = 0; j < attrs.size; j++){
                    var alias = attrs.getKeyAt(j);
                    alias = parameter.attributeAlias.get(alias, alias);
                    if(mesh.name != null && parameter.skipAttribute.get(mesh.name, ObjectSet::new).contains(alias)){
                        attributes.add((VertexAttribute)null);
                        continue;
                    }

                    var accessor = spec.accessors[attrs.getValueAt(j).asInt()];
                    if(verticesCount != -1 && accessor.count != verticesCount){
                        throw new IllegalArgumentException("Vertices count mismatch, found accessor with count " + accessor.count + " instead of " + verticesCount);
                    }else{
                        verticesCount = accessor.count;
                    }

                    var attr = new VertexAttribute(switch(accessor.type){
                        case scalar -> 1;
                        case vec2 -> 2;
                        case vec3 -> 3;
                        case vec4, mat2 -> 4;
                        case mat3 -> 9;
                        case mat4 -> 16;
                    }, accessor.componentType, accessor.normalized, alias);
                    verticesLen += verticesCount * attr.size;

                    attributes.add(attr);
                }

                var vertexBuffer = Buffers.newUnsafeByteBuffer(verticesLen);
                vertexBuffer.order(ByteOrder.LITTLE_ENDIAN);
                for(int j = 0; j < verticesCount; j++){
                    for(int k = 0; k < attrs.size; k++){
                        var attr = attributes.get(k);
                        if(attr == null) continue;

                        var accessor = spec.accessors[attrs.getValueAt(k).asInt()];
                        if(accessor.bufferView == -1){
                            // TODO Handle sparse accessors.
                            vertexBuffer.put(new byte[attr.size]);
                        }else{
                            var view = bufferViews[accessor.bufferView];
                            view.clear();
                            view.limit(accessor.byteOffset + (j + 1) * attr.size);
                            view.position(accessor.byteOffset + j * attr.size);
                            vertexBuffer.put(view);
                        }
                    }
                }

                int indicesCount = 0;
                ByteBuffer indexBuffer = null;
                if(primitives.indices != -1){
                    var accessor = spec.accessors[primitives.indices];
                    if(accessor.type != AccessorType.scalar) throw new IllegalArgumentException("Indices accessor must be scalar.");

                    indexBuffer = Buffers.newUnsafeByteBuffer((indicesCount = accessor.count) * Short.BYTES);
                    indexBuffer.order(ByteOrder.LITTLE_ENDIAN);
                    if(accessor.bufferView == -1){
                        indexBuffer.put(new byte[indicesCount * Short.BYTES]);
                    }else{
                        var view = bufferViews[accessor.bufferView];
                        switch(accessor.componentType){
                            case Gl.byteV, Gl.unsignedByte -> {
                                byte[] indices = new byte[indicesCount];
                                view.clear();
                                view.limit(accessor.byteOffset + indicesCount * Byte.BYTES);
                                view.position(accessor.byteOffset);
                                view.get(indices);

                                var dst = indexBuffer.asShortBuffer();
                                for(byte index : indices) dst.put(index);
                            }
                            case Gl.shortV, Gl.unsignedShort -> {
                                view.clear();
                                view.limit(accessor.byteOffset + indicesCount * Short.BYTES);
                                view.position(accessor.byteOffset);

                                indexBuffer.asShortBuffer().put(view.asShortBuffer());
                            }
                            case Gl.unsignedInt -> {
                                int[] indices = new int[indicesCount];
                                view.clear();
                                view.limit(accessor.byteOffset + indicesCount * Integer.BYTES);
                                view.position(accessor.byteOffset);
                                view.asIntBuffer().get(indices);

                                var dst = indexBuffer.asShortBuffer();
                                for(int index : indices) dst.put((short)index);
                            }
                            default -> throw new IllegalArgumentException("Indices accessor component type must be integer.");
                        }
                    }
                }

                var queue = cont.containers[m] = new MeshQueue();
                queue.vertices = vertexBuffer;
                queue.vertices.clear();
                queue.maxVertices = verticesCount;

                queue.indices = indexBuffer;
                queue.indices.clear();
                queue.maxIndices = indicesCount;

                queue.attributes = attributes.retainAll(Objects::nonNull).toArray();
                queue.mode = primitives.mode;
            }
        }
    }

    @Override
    public Scenes3D loadSync(AssetManager manager, String fileName, Fi file, Scenes3DParameter parameter){
        var out = asset;
        for(var cont : meshes){
            var set = new MeshSet();
            set.name = cont.name;

            for(var queue : cont.containers){
                var mesh = new Mesh(true, queue.maxVertices, queue.maxIndices, queue.attributes);
                {
                    FloatBuffer src = queue.vertices.asFloatBuffer(), dst = mesh.getVerticesBuffer();
                    src.clear();
                    dst.clear();

                    dst.put(src);
                    dst.clear();
                }

                {
                    ShortBuffer src = queue.indices.asShortBuffer(), dst = mesh.getIndicesBuffer();
                    src.clear();
                    dst.clear();

                    dst.put(src);
                    dst.clear();
                }

                Buffers.disposeUnsafeByteBuffer(queue.vertices);
                Buffers.disposeUnsafeByteBuffer(queue.indices);
                set.containers.add(new MeshContainer(mesh, queue.mode));
            }

            out.meshes.add(set);
            if(!set.name.isEmpty()) out.meshNames.put(set.name, out.meshes.size - 1);
        }

        asset = null;
        meshes = null;
        return out;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Seq<AssetDescriptor> getDependencies(String fileName, Fi file, Scenes3DParameter parameter){
        return null;
    }

    public static class Scenes3DParameter extends AssetLoaderParameters<Scenes3D>{
        public Scenes3D asset;
        public StringMap attributeAlias = StringMap.of(
            "COLOR_0", VertexAttribute.color.alias,
            "POSITION", VertexAttribute.position3.alias,
            "NORMAL", VertexAttribute.normal.alias,
            "TEXCOORD_0", VertexAttribute.texCoords.alias
        );
        public ObjectMap<String, ObjectSet<String>> skipAttribute = new ObjectMap<>();

        public Scenes3DParameter(){
            this(null);
        }

        public Scenes3DParameter(Scenes3D asset){
            this.asset = asset;
        }

        public Scenes3DParameter alias(String from, String to){
            attributeAlias.put(from, to);
            return this;
        }

        public Scenes3DParameter skip(String mesh, String attribute){
            skipAttribute.get(mesh, ObjectSet::new).add(attribute);
            return this;
        }
    }

    protected static class MeshContainerQueue{
        public String name;
        public MeshQueue[] containers;

        public static class MeshQueue{
            public ByteBuffer vertices, indices;
            public VertexAttribute[] attributes;
            public int maxVertices, maxIndices, mode;
        }
    }
}
