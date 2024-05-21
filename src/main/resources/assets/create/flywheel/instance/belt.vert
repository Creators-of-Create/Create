#include "flywheel:util/quaternion.glsl"
#include "flywheel:util/matrix.glsl"

void flw_instanceVertex(in FlwInstance instance) {
    flw_vertexPos = vec4(rotateByQuaternion(flw_vertexPos.xyz - .5, instance.rotation) + instance.pos + .5, 1.);

    flw_vertexNormal = rotateByQuaternion(flw_vertexNormal, instance.rotation);

    float scrollSize = instance.scrollTexture.w - instance.scrollTexture.y;
    float scroll = fract(instance.speed * flw_renderTicks / (31.5 * 16.) + instance.offset) * scrollSize * instance.scrollMult;

    flw_vertexTexCoord = flw_vertexTexCoord - instance.sourceTexture + instance.scrollTexture.xy + vec2(0, scroll);
    flw_vertexLight = vec2(instance.light) / 256.;
    flw_vertexOverlay = instance.overlay;

    #if defined(DEBUG_RAINBOW)
    flw_vertexColor = instance.color;
    #endif
}
