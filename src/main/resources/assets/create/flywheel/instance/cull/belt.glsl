#define PI 3.1415926538

#include "flywheel:util/quaternion.glsl"
#include "flywheel:util/matrix.glsl"

const float uTime = 0.;

void flw_transformBoundingSphere(in FlwInstance instance, inout vec3 center, inout float radius) {
    // FIXME: this is incorrect, but it compiles
    center = rotateVertexByQuat(center - .5, instance.rotation) + instance.pos + .5;
}
