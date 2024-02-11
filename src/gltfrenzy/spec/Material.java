package gltfrenzy.spec;

import arc.graphics.*;
import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Named
@Spec({
    @Prop(name = "pbrMetallicRoughness", type = MaterialMetallicRoughness.class),
    @Prop(name = "normalTexture", type = MaterialNormalTexture.class),
    @Prop(name = "occlusionTexture", type = MaterialOcclusionTexture.class),
    @Prop(name = "emissiveTexture", type = TextureInfo.class),
    @Prop(name = "emissiveFactor", type = Color.class, def = @Def(value = "$T.clear.cpy()", args = Color.class)),
    @Prop(name = "alphaMode", type = MaterialAlphaMode.class, def = @Def(value = "$T.opaque", args = MaterialAlphaMode.class)),
    @Prop(name = "alphaCutoff", type = float.class, def = @Def("0.5f")),
    @Prop(name = "doubleSided", type = boolean.class, def = @Def("false"))
})
class Material{}
