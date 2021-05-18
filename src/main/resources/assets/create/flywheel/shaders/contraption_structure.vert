#define PI 3.1415926538

#flwbuiltins
#flwinclude <"create:core/matutils.glsl">
#flwinclude <"create:core/diffuse.glsl">

#[VertexData]
struct Vertex {
    vec3 pos;
    vec3 normal;
    vec2 texCoords;
    vec4 color;
    vec2 modelLight;
};

#flwinclude <"create:data/blockfragment.glsl">

BlockFrag FLWMain(Vertex v) {
    vec4 worldPos = vec4(v.pos, 1.);
    vec3 norm = v.normal;

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    BlockFrag b;
    b.diffuse = diffuse(norm);
    b.color = v.color / diffuse(v.normal);
    b.texCoords = v.texCoords;
    b.light = v.modelLight;

    return b;
}
