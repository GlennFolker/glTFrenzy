package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Spec({
    @Prop(name = "count", type = int.class, required = true),
    @Prop(name = "indices", type = AccessorSparseIndices.class, required = true),
    @Prop(name = "values", type = AccessorSparseValues.class, required = true)
})
class AccessorSparse{}
