#flwbuiltins
#flwinclude <"flywheel:core/matutils.glsl">
#flwinclude <"flywheel:core/quaternion.glsl">
#flwinclude <"flywheel:core/diffuse.glsl">

#[InstanceData]
struct Oriented {
    vec2 light;
    vec4 color;
    vec3 pos;
    vec3 pivot;
    vec4 rotation;
};

#flwinclude <"flywheel:data/modelvertex.glsl">
#flwinclude <"flywheel:data/blockfragment.glsl">

BlockFrag FLWMain(Vertex v, Oriented o) {
    vec4 worldPos = vec4(rotateVertexByQuat(v.pos - o.pivot, o.rotation) + o.pivot + o.pos, 1.);

    vec3 norm = rotateVertexByQuat(v.normal, o.rotation);

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    BlockFrag b;
    b.diffuse = diffuse(norm);
    b.texCoords = v.texCoords;
    b.light = o.light;
    #if defined(NORMAL_DEBUG)
    b.color = vec4(norm, 1.);
    #else
    b.color = o.color;
    #endif
    return b;
}
