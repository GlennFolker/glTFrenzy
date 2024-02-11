package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Named
@Spec({
    @Prop(name = "uri", type = String.class),
    @Prop(name = "byteLength", type = int.class, required = true),
})
class Buffer{}
