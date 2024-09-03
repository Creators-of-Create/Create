#include "flywheel:util/matrix.glsl"

const float uTime = 0.;

mat3 kineticRotation(float offset, float speed, vec3 axis) {
    float degrees = offset + flw_renderTicks * speed * 3./10.;
    return rotationDegrees(axis, degrees);
}

void flw_instanceVertex(in FlwInstance instance) {
    mat3 spin = kineticRotation(instance.offset, instance.speed, instance.axis);

    vec3 worldPos = spin * (flw_vertexPos.xyz - .5);
    flw_vertexPos.xyz = worldPos.xyz + instance.pos + .5;

    flw_vertexNormal = spin * flw_vertexNormal;
    flw_vertexLight = vec2(instance.light) / 256.;
    flw_vertexOverlay = instance.overlay;

    #if defined(DEBUG_RAINBOW)
    flw_vertexColor = instance.color;
    #endif
}
