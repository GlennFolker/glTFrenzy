package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Named
@Spec({
    @Prop(name = "primitives", type = MeshPrimitive.class, required = true),
    @Prop(name = "weights", type = float[].class),
})
class Mesh{}
