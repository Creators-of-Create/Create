#flwbuiltins
#flwinclude <"create:core/diffuse.glsl">

#[InstanceData]
struct Instance {
    vec2 light;
    vec4 color;
    mat4 transform;
    mat3 normalMat;
};

#flwinclude <"create:data/modelvertex.glsl">
#flwinclude <"create:data/blockfragment.glsl">

BlockFrag FLWMain(Vertex v, Instance i) {
    vec4 worldPos = i.transform * vec4(v.pos, 1.);

    vec3 norm = i.normalMat * v.normal;

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    norm = normalize(norm);

    BlockFrag b;
    b.diffuse = diffuse(norm);
    b.texCoords = v.texCoords;
    b.light = i.light;
    b.color = i.color;
    return b;
}
