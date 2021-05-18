#define PI 3.1415926538

#flwbuiltins
#flwinclude <"create:core/matutils.glsl">
#flwinclude <"create:core/diffuse.glsl">

#[InstanceData]
struct Rotating {
    vec2 light;
    vec4 color;
    vec3 pos;
    float speed;
    float offset;
    vec3 axis;
};

#flwinclude <"create:data/modelvertex.glsl">
#flwinclude <"create:data/blockfragment.glsl">

mat4 kineticRotation(float offset, float speed, vec3 axis) {
    float degrees = offset + uTime * speed * -3./10.;
    float angle = fract(degrees / 360.) * PI * 2.;

    return rotate(axis, angle);
}

BlockFrag FLWMain(Vertex v, Rotating instance) {
    mat4 kineticRotation = kineticRotation(instance.offset, instance.speed, instance.axis);

    vec4 worldPos = vec4(v.pos - .5, 1.);
    worldPos *= kineticRotation;
    worldPos += vec4(instance.pos + .5, 0.);

    vec3 norm = modelToNormal(kineticRotation) * v.normal;

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    BlockFrag b;
    b.diffuse = diffuse(norm);
    b.texCoords = v.texCoords;
    b.light = instance.light;

    #if defined(RAINBOW_DEBUG)
    b.color = instance.color;
    #else
    b.color = vec4(1.);
    #endif

    return b;
}
