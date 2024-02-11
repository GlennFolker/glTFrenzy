package gltfrenzy.spec;

import arc.graphics.*;
import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Named
@Spec({
    @Prop(name = "magFilter", type = int.class),
    @Prop(name = "minFilter", type = int.class),
    @Prop(name = "wrapS", type = int.class, def = @Def(value = "$T.repeat", args = Gl.class)),
    @Prop(name = "wrapT", type = int.class, def = @Def(value = "$T.repeat", args = Gl.class))
})
class Sampler{}
