#include "flywheel:util/quaternion.glsl"

void flw_transformBoundingSphere(in FlwInstance instance, inout vec3 center, inout float radius) {
    // FIXME: this is incorrect, but it compiles
    center = rotateByQuaternion(center - .5, instance.rotation) + instance.pos + .5;
}
