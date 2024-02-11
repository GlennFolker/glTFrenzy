package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Spec({
    @Prop(name = "index", type = int.class, required = true),
    @Prop(name = "texCoord", type = int.class, def = @Def("0"))
})
class TextureInfo{}
