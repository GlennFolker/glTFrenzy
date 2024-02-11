package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Named
@Spec({
    @Prop(name = "buffer", type = int.class, required = true),
    @Prop(name = "byteOffset", type = int.class, def = @Def("0")),
    @Prop(name = "byteLength", type = int.class, required = true),
    @Prop(name = "byteStride", type = int.class),
    @Prop(name = "target", type = int.class),
})
class BufferView{}
