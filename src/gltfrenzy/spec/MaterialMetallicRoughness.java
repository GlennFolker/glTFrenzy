package gltfrenzy.spec;

import arc.graphics.*;
import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Spec({
    @Prop(name = "baseColorFactor", type = Color.class, def = @Def(value = "$T.white.cpy()", args = Color.class)),
    @Prop(name = "baseColorTexture", type = TextureInfo.class),
    @Prop(name = "metallicFactor", type = float.class, def = @Def("1f")),
    @Prop(name = "roughnessFactor", type = float.class, def = @Def("1f")),
    @Prop(name = "metallicRoughnessTexture", type = TextureInfo.class)
})
class MaterialMetallicRoughness{}
