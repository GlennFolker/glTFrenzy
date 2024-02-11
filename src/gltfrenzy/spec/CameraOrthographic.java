package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Spec({
    @Prop(name = "xmag", type = float.class, required = true),
    @Prop(name = "ymag", type = float.class, required = true),
    @Prop(name = "zfar", type = float.class, required = true),
    @Prop(name = "znear", type = float.class, required = true),
})
class CameraOrthographic{}
