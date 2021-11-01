#define PI 3.1415926538

#use "flywheel:core/quaternion.glsl"
#use "flywheel:core/matutils.glsl"
#use "flywheel:core/diffuse.glsl"

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

#use "flywheel:data/modelvertex.glsl"
#use "flywheel:block.frag"

#if defined(VERTEX_SHADER)
BlockFrag vertex(Vertex v, Belt instance) {
    vec3 rotated = rotateVertexByQuat(v.pos - .5, instance.rotation) + instance.pos + .5;

    vec4 worldPos = vec4(rotated, 1.);

    vec3 norm = rotateVertexByQuat(v.normal, instance.rotation);

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    float scrollSize = instance.scrollTexture.w - instance.scrollTexture.y;
    float scroll = fract(instance.speed * uTime / (31.5 * 16.) + instance.offset) * scrollSize * instance.scrollMult;

    BlockFrag b;
    b.diffuse = diffuse(norm);
    b.texCoords = v.texCoords - instance.sourceTexture + instance.scrollTexture.xy + vec2(0, scroll);
    b.light = instance.light;

    #if defined(DEBUG_RAINBOW)
    b.color = instance.color;
    #elif defined(DEBUG_NORMAL)
    b.color = vec4(norm, 1.);
    #else
    b.color = vec4(1.);
    #endif

    return b;
}
#endif
