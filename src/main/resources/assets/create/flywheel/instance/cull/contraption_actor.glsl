#include "flywheel:util/matrix.glsl"
#include "flywheel:util/quaternion.glsl"

const float uTime = 0.;

void flw_transformBoundingSphere(in FlwInstance instance, inout vec3 center, inout float radius) {
    // FIXME: this is incorrect, but it compiles
    float degrees = instance.offset + uTime * instance.speed / 20.;

    vec4 kineticRot = quat(instance.axis, degrees);
    vec3 rotated = rotateVertexByQuat(center - instance.rotationCenter, kineticRot) + instance.rotationCenter;

    center = rotateVertexByQuat(rotated - .5, instance.rotation) + instance.pos + .5;
}
