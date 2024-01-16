#define PI 3.1415926538

#include "flywheel:util/quaternion.glsl"
#include "flywheel:util/matrix.glsl"

const float uTime = 0.;


void flw_instanceVertex(in FlwInstance instance) {
    flw_vertexPos = vec4(rotateVertexByQuat(flw_vertexPos.xyz - .5, instance.rotation) + instance.pos + .5, 1.);

    flw_vertexNormal = rotateVertexByQuat(flw_vertexNormal, instance.rotation);

    float scrollSize = instance.scrollTexture.w - instance.scrollTexture.y;
    float scroll = fract(instance.speed * uTime / (31.5 * 16.) + instance.offset) * scrollSize * instance.scrollMult;

    flw_vertexTexCoord = flw_vertexTexCoord - instance.sourceTexture + instance.scrollTexture.xy + vec2(0, scroll);
    flw_vertexLight = instance.light;

    #if defined(DEBUG_RAINBOW)
    flw_vertexColor = instance.color;
    #endif
}
