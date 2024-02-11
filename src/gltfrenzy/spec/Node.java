package gltfrenzy.spec;

import arc.math.geom.*;
import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Named
@Spec({
    @Prop(name = "camera", type = int.class),
    @Prop(name = "children", type = int[].class),
    @Prop(name = "skin", type = int.class),
    @Prop(name = "matrix", type = Mat3D.class, def = @Def(value = "new $T()", args = Mat3D.class)),
    @Prop(name = "mesh", type = int.class),
    @Prop(name = "rotation", type = Quat.class, def = @Def(value = "new $T()", args = Quat.class)),
    @Prop(name = "scale", type = Vec3.class, def = @Def(value = "new $T(1f, 1f, 1f)", args = Vec3.class)),
    @Prop(name = "translation", type = Vec3.class, def = @Def(value = "new $T()", args = Vec3.class)),
    @Prop(name = "weights", type = float[].class)
})
class Node{}
