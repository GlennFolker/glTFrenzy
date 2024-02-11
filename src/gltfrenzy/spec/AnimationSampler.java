package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Spec({
    @Prop(name = "input", type = int.class, required = true),
    @Prop(name = "interpolation", type = AnimationInterpolation.class, def = @Def(value = "$T.linear", args = AnimationInterpolation.class)),
    @Prop(name = "output", type = int.class, required = true)
})
class AnimationSampler{}
