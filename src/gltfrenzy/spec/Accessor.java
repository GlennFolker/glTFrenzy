package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Named
@Spec({
    @Prop(name = "bufferView", type = int.class),
    @Prop(name = "byteOffset", type = int.class, def = @Def("0")),
    @Prop(name = "componentType", type = int.class, required = true),
    @Prop(name = "normalized", type = boolean.class, def = @Def("false")),
    @Prop(name = "type", type = String.class, required = true),
    @Prop(name = "max", type = float[].class),
    @Prop(name = "min", type = float[].class),
    @Prop(name = "sparse", type = AccessorSparse.class)
})
class Accessor{}
