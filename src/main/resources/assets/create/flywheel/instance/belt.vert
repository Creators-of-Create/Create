#define PI 3.1415926538

#include "flywheel:core/quaternion.glsl"
#include "flywheel:core/matutils.glsl"

struct Belt {
    vec2 light;
    vec4 color;
    vec3 pos;
    float speed;
    float offset;
    vec4 rotation;
    vec2 sourceTexture;
    vec4 scrollTexture;
    float scrollMult;
};


void vertex(inout Vertex v, Belt instance) {
    v.pos = rotateVertexByQuat(v.pos - .5, instance.rotation) + instance.pos + .5;

    v.normal = rotateVertexByQuat(v.normal, instance.rotation);

    float scrollSize = instance.scrollTexture.w - instance.scrollTexture.y;
    float scroll = fract(instance.speed * uTime / (31.5 * 16.) + instance.offset) * scrollSize * instance.scrollMult;

    v.texCoords = v.texCoords - instance.sourceTexture + instance.scrollTexture.xy + vec2(0, scroll);
    v.light = instance.light;

    #if defined(DEBUG_RAINBOW)
    v.color = instance.color;
    #endif
}
