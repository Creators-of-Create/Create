#include "flywheel:util/quaternion.glsl"

void flw_transformBoundingSphere(in FlwInstance instance, inout vec3 center, inout float radius) {
    // FIXME: this is incorrect, but it compiles
    float degrees = instance.offset + flw_renderSeconds * instance.speed;

    vec4 kineticRot = quaternion(instance.axis, degrees);
    vec3 rotated = rotateByQuaternion(center - instance.rotationCenter, kineticRot) + instance.rotationCenter;

    center = rotateByQuaternion(rotated - .5, instance.rotation) + instance.pos + .5;
}
