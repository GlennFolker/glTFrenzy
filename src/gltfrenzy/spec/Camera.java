package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Named
@Spec({
    @Prop(name = "perspective", type = CameraPerspective.class),
    @Prop(name = "orthographic", type = CameraOrthographic.class),
    @Prop(name = "type", type = CameraProjection.class)
})
class Camera{}
