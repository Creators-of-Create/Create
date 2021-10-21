#define PI 3.1415926538

#flwbuiltins
#flwinclude <"flywheel:core/matutils.glsl">
#flwinclude <"flywheel:core/quaternion.glsl">
#flwinclude <"flywheel:core/diffuse.glsl">

#[InstanceData]
struct Actor {
    vec3 pos;
    vec2 light;
    float offset;
    vec3 axis;
    vec4 rotation;
    vec3 rotationCenter;
    float speed;
};

#flwinclude <"flywheel:data/modelvertex.glsl">
#flwinclude <"flywheel:data/blockfragment.glsl">

BlockFrag FLWMain(Vertex v, Actor instance) {
    float degrees = instance.offset + uTime * instance.speed / 20.;
    //float angle = fract(degrees / 360.) * PI * 2.;

    vec4 kineticRot = quat(instance.axis, degrees);
    vec3 rotated = rotateVertexByQuat(v.pos - instance.rotationCenter, kineticRot) + instance.rotationCenter;

    vec4 worldPos = vec4(rotateVertexByQuat(rotated - .5, instance.rotation) + instance.pos + .5, 1.);
    vec3 norm = rotateVertexByQuat(rotateVertexByQuat(v.normal, kineticRot), instance.rotation);

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    BlockFrag b;
    b.diffuse = diffuse(norm);
    b.texCoords = v.texCoords;
    b.light = instance.light;

    #if defined(DEBUG_NORMAL)
    b.color = vec4(norm, 1.);
    #else
    b.color = vec4(1.);
    #endif

    return b;
}
