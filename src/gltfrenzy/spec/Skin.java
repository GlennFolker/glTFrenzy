package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Named
@Spec({
    @Prop(name = "inverseBindMatrices", type = int.class),
    @Prop(name = "skeleton", type = int.class),
    @Prop(name = "joints", type = int[].class, required = true)
})
class Skin{}
