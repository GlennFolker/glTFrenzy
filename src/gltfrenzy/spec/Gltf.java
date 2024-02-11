package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Spec({
    @Prop(name = "extensionsUsed", type = String[].class),
    @Prop(name = "extensionsRequired", type = String[].class),
    @Prop(name = "accessors", type = Accessor[].class),
    @Prop(name = "animations", type = Animation[].class),
    @Prop(name = "asset", type = Asset.class, required = true),
    @Prop(name = "buffers", type = Buffer[].class),
    @Prop(name = "bufferViews", type = BufferView[].class),
    @Prop(name = "cameras", type = Camera[].class),
    @Prop(name = "images", type = Image[].class),
    @Prop(name = "materials", type = Material[].class),
    @Prop(name = "meshes", type = Mesh[].class),
    @Prop(name = "nodes", type = Node[].class),
    @Prop(name = "samplers", type = Sampler[].class),
    @Prop(name = "scene", type = int.class),
    @Prop(name = "scenes", type = Scene[].class),
    @Prop(name = "skins", type = Skin[].class),
    @Prop(name = "textures", type = Texture[].class)
})
class Gltf{}
