package gltfrenzy.model;

import arc.struct.*;
import arc.util.*;

/**
 * A <a href=https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html>glTF 2.0</a> scene implementation.
 * @author GlennFolker
 */
public class Scenes3D implements Disposable{
    public final Seq<MeshSet> meshes = new Seq<>();
    public final ObjectMap<String, MeshSet> meshNames = new ObjectMap<>();

    public final Seq<Node> nodes = new Seq<>();
    public final ObjectMap<String, Node> nodeNames = new ObjectMap<>();
    public final IntSeq rootNodes = new IntSeq();

    @Override
    public void dispose(){
        meshes.each(MeshSet::dispose);
        meshes.clear();
    }
}
