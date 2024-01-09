#define PI 3.1415926538

#include "flywheel:core/matutils.glsl"
#include "flywheel:core/quaternion.glsl"

struct Actor {
    vec3 pos;
    vec2 light;
    float offset;
    vec3 axis;
    vec4 rotation;
    vec3 rotationCenter;
    float speed;
};

void vertex(inout Vertex v, Actor instance) {
    float degrees = instance.offset + uTime * instance.speed / 20.;
    //float angle = fract(degrees / 360.) * PI * 2.;

    vec4 kineticRot = quat(instance.axis, degrees);
    vec3 rotated = rotateVertexByQuat(v.pos - instance.rotationCenter, kineticRot) + instance.rotationCenter;

    v.pos = rotateVertexByQuat(rotated - .5, instance.rotation) + instance.pos + .5;
    v.normal = rotateVertexByQuat(rotateVertexByQuat(v.normal, kineticRot), instance.rotation);
    v.light = instance.light;
}
