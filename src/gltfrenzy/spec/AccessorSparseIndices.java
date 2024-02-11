package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Spec({
    @Prop(name = "bufferView", type = int.class, required = true),
    @Prop(name = "byteOffset", type = int.class, def = @Def("0")),
    @Prop(name = "componentType", type = int.class, required = true)
})
class AccessorSparseIndices{}
