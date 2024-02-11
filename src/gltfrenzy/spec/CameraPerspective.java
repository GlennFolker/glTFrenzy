package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Spec({
    @Prop(name = "aspectRatio", type = float.class),
    @Prop(name = "yfov", type = float.class, required = true),
    @Prop(name = "zfar", type = float.class),
    @Prop(name = "znear", type = float.class, required = true),
})
class CameraPerspective{}
