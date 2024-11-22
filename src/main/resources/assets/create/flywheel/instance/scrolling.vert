#include "flywheel:util/quaternion.glsl"
#include "flywheel:util/matrix.glsl"

void flw_instanceVertex(in FlwInstance instance) {
    flw_vertexPos = vec4(rotateByQuaternion(flw_vertexPos.xyz - .5, instance.rotation) + instance.pos + .5, 1.);

    flw_vertexNormal = rotateByQuaternion(flw_vertexNormal, instance.rotation);

    vec2 scroll = fract(instance.speed * flw_renderTicks) * instance.scale;

    flw_vertexTexCoord = flw_vertexTexCoord + instance.diff + scroll;
    flw_vertexLight = vec2(instance.light) / 256.;
    flw_vertexOverlay = instance.overlay;
}
