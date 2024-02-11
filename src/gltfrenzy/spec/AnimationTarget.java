package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Spec({
    @Prop(name = "node", type = int.class),
    @Prop(name = "path", type = AnimationPath.class, required = true)
})
class AnimationTarget{}
