package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Spec({
    @Prop(name = "bufferView", type = int.class),
    @Prop(name = "byteOffset", type = int.class, def = @Def("0")),
})
class AccessorSparseValues{}
