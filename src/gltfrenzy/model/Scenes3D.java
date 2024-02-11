package gltfrenzy.model;

import arc.struct.*;
import arc.util.*;

/**
 * A <a href=https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html>glTF 2.0</a> scene implementation.
 * @author GlennFolker
 */
public class Scenes3D implements Disposable{
    public final Seq<MeshSet> meshes = new Seq<>();
    public final ObjectIntMap<String> meshNames = new ObjectIntMap<>();

    @Override
    public void dispose(){
        meshes.each(MeshSet::dispose);
    }
}
