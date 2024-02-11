package gltfrenzy.spec;

import gltf.annotations.*;

@Spec
enum AnimationInterpolation{
    linear,
    step,
    @Alias("cubicspline")
    spline,
}
