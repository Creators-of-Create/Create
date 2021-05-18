#flwbuiltins
#flwinclude <"create:core/matutils.glsl">
#flwinclude <"create:core/quaternion.glsl">
#flwinclude <"create:core/diffuse.glsl">

#[InstanceData]
struct Oriented {
    vec2 light;
    vec4 color;
    vec3 pos;
    vec3 pivot;
    vec4 rotation;
};

#flwinclude <"create:data/modelvertex.glsl">
#flwinclude <"create:data/blockfragment.glsl">

BlockFrag FLWMain(Vertex v, Oriented o) {
    vec4 worldPos = vec4(rotateVertexByQuat(v.pos - o.pivot, o.rotation) + o.pivot + o.pos, 1.);

    vec3 norm = rotateVertexByQuat(v.normal, o.rotation);

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    BlockFrag b;
    b.diffuse = diffuse(norm);
    b.texCoords = v.texCoords;
    b.light = o.light;
    b.color = o.color;
    return b;
}
