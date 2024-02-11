package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Spec({
    @Prop(name = "copyright", type = String.class),
    @Prop(name = "generator", type = String.class),
    @Prop(name = "version", type = String.class, required = true),
    @Prop(name = "minVersion", type = String.class),
})
class Asset{}
