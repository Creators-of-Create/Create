#flwbuiltins
#flwinclude <"flywheel:core/matutils.glsl">
#flwinclude <"flywheel:core/quaternion.glsl">
#flwinclude <"flywheel:core/diffuse.glsl">

#[InstanceData]
struct Oriented {
// each vec 4 is 2 light coords packed <lo y, hi y>
//               x   z
    vec4 lightA;// lo, lo
    vec4 lightB;// hi, lo
    vec4 lightC;// hi, hi
    vec4 lightD;// lo, hi

    vec3 loCorner;
    vec3 size;

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

    // manual trilinear interpolation
    vec3 lightPos = (worldPos.xyz - o.loCorner) / o.size;

    vec4 lightLoZ = mix(lightA, lightB, lightPos.x);// lo z
    vec4 lightHiZ = mix(lightD, lightC, lightPos.x);// hi z

    vec4 lightE = mix(lightLoZ, lightHiZ, lightPos.z);// <lo y, hi y>

    vec2 lightCoord = mix(lightE.xy, lightE.zw, lightPos.y);

    BlockFrag b;
    b.diffuse = diffuse(norm);
    b.texCoords = v.texCoords;
    b.light = lightCoord;
    #if defined(DEBUG_NORMAL)
    b.color = vec4(norm, 1.);
    #else
    b.color = o.color;
    #endif
    return b;
}
