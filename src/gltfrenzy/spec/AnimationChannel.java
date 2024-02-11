package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Spec({
    @Prop(name = "sampler", type = int.class, required = true),
    @Prop(name = "target", type = AnimationChannel.class, required = true)
})
class AnimationChannel{}
