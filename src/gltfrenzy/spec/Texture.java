package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Named
@Spec({
    @Prop(name = "sampler", type = int.class),
    @Prop(name = "source", type = int.class)
})
class Texture{}
