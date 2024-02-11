package gltfrenzy.spec;

import arc.graphics.*;
import arc.util.serialization.Jval.*;
import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Spec({
    @Prop(name = "attributes", type = JsonMap.class, required = true),
    @Prop(name = "indices", type = int.class),
    @Prop(name = "material", type = int.class),
    @Prop(name = "mode", type = int.class, def = @Def(value = "$T.triangles", args = Gl.class)),
    @Prop(name = "targets", type = JsonMap[].class)
})
class MeshPrimitive{}
