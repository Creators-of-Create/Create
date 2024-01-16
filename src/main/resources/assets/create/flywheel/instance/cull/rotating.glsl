#define PI 3.1415926538

#include "flywheel:util/matrix.glsl"

const float uTime = 0.;

mat4 kineticRotation(float offset, float speed, vec3 axis) {
    float degrees = offset + uTime * speed * 3./10.;
    float angle = fract(degrees / 360.) * PI * 2.;

    return rotate(axis, angle);
}

void flw_transformBoundingSphere(in FlwInstance instance, inout vec3 center, inout float radius) {
    // FIXME: this is incorrect, but it compiles
    mat4 spin = kineticRotation(instance.offset, instance.speed, instance.axis);

    vec4 worldPos = spin * vec4(center - .5, 1.);
    center = worldPos.xyz + instance.pos + .5;
}
