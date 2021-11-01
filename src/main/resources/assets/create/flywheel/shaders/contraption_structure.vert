#define PI 3.1415926538

#use "flywheel:core/matutils.glsl"
#use "flywheel:core/diffuse.glsl"

struct Vertex {
    vec3 pos;
    vec3 normal;
    vec2 texCoords;
    vec4 color;
    vec2 modelLight;
};

#use "flywheel:block.frag"

#if defined(VERTEX_SHADER)
BlockFrag vertex(Vertex v) {
    vec4 worldPos = vec4(v.pos, 1.);
    vec3 norm = v.normal;

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    BlockFrag b;
    b.diffuse = diffuse(norm);
    b.texCoords = v.texCoords;
    b.light = v.modelLight;

    #if defined(DEBUG_NORMAL)
    b.color = vec4(norm, 1.);
    #else
    b.color = vec4(v.color.rgb / diffuse(v.normal), v.color.a);
    #endif

    return b;
}
#endif
