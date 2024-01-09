#define PI 3.1415926538

#include "flywheel:core/matutils.glsl"
#include "flywheel:core/quaternion.glsl"

struct Flap {
    vec3 instancePos;
    vec2 light;
    vec3 segmentOffset;
    vec3 pivot;
    float horizontalAngle;
    float intensity;
    float flapScale;
    float flapness;
};

float toRad(float degrees) {
    return fract(degrees / 360.) * PI * 2.;
}

float getFlapAngle(float flapness, float intensity, float scale) {
    float absFlap = abs(flapness);

    float angle = sin((1. - absFlap) * PI * intensity) * 30. * flapness * scale;

    float halfAngle = angle * 0.5;

    float which = step(0., flapness); // 0 if negative, 1 if positive
    float degrees = which * halfAngle + (1. - which) * angle; // branchless conditional multiply

    return degrees;
}

void vertex(inout Vertex v, Flap flap) {
    float flapAngle = getFlapAngle(flap.flapness, flap.intensity, flap.flapScale);

    vec4 orientation = quat(vec3(0., 1., 0.), -flap.horizontalAngle);
    vec4 flapRotation = quat(vec3(1., 0., 0.), flapAngle);

    vec3 rotated = rotateVertexByQuat(v.pos - flap.pivot, flapRotation) + flap.pivot + flap.segmentOffset;
    rotated = rotateVertexByQuat(rotated - .5, orientation) + flap.instancePos + .5;

    v.pos = rotated;
    v.normal = rotateVertexByQuat(rotateVertexByQuat(v.normal, flapRotation), orientation);
    v.light = flap.light;
}
