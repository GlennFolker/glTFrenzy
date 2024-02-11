package gltfrenzy.spec;

import gltf.annotations.*;
import gltf.annotations.Spec.*;

@Named
@Spec({
    @Prop(name = "uri", type = String.class),
    @Prop(name = "mimeType", type = ImageType.class),
    @Prop(name = "bufferView", type = int.class),
})
class Image{}
