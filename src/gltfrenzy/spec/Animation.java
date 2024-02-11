package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Named
@Spec({
    @Prop(name = "channels", type = AnimationChannel[].class, required = true),
    @Prop(name = "samplers", type = AnimationSampler[].class, required = true)
})
class Animation{}
