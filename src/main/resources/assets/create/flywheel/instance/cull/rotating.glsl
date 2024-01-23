#include "flywheel:util/matrix.glsl"

mat3 kineticRotation(float offset, float speed, vec3 axis) {
    float degrees = offset + flw_renderTicks * speed * 3./10.;

    return rotationDegrees(axis, degrees);
}

void flw_transformBoundingSphere(in FlwInstance instance, inout vec3 center, inout float radius) {
    // FIXME: this is incorrect, but it compiles
    mat3 spin = kineticRotation(instance.offset, instance.speed, instance.axis);

    vec3 worldPos = spin * (center - .5);
    center = worldPos + instance.pos + .5;
}
