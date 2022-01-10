#define PI 3.1415926538

#use "flywheel:core/matutils.glsl"

struct Rotating {
    vec2 light;
    vec4 color;
    vec3 pos;
    float speed;
    float offset;
    vec3 axis;
};

mat4 kineticRotation(float offset, float speed, vec3 axis) {
    float degrees = offset + uTime * speed * 3./10.;
    float angle = fract(degrees / 360.) * PI * 2.;

    return rotate(axis, angle);
}

void vertex(inout Vertex v, Rotating instance) {
    mat4 spin = kineticRotation(instance.offset, instance.speed, instance.axis);

    vec4 worldPos = spin * vec4(v.pos - .5, 1.);
    v.pos = worldPos.xyz + instance.pos + .5;

    v.normal = modelToNormal(spin) * v.normal;
    v.light = instance.light;

    #if defined(DEBUG_RAINBOW)
    v.color = instance.color;
    #endif
}
