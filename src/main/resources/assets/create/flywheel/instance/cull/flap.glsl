#define PI 3.1415926538

#include "flywheel:util/quaternion.glsl"

float getFlapAngle(float flapness, float intensity, float scale) {
    float absFlap = abs(flapness);

    float angle = sin((1. - absFlap) * PI * intensity) * 30. * flapness * scale;

    float halfAngle = angle * 0.5;

    float which = step(0., flapness); // 0 if negative, 1 if positive
    float degrees = which * halfAngle + (1. - which) * angle; // branchless conditional multiply

    return degrees;
}

void flw_transformBoundingSphere(in FlwInstance flap, inout vec3 center, inout float radius) {
    // FIXME: this is incorrect, but it compiles
    float flapAngle = getFlapAngle(flap.flapness, flap.intensity, flap.flapScale);

    vec4 orientation = quaternion(vec3(0., 1., 0.), -flap.horizontalAngle);
    vec4 flapRotation = quaternion(vec3(1., 0., 0.), flapAngle);

    vec3 rotated = rotateByQuaternion(center - flap.pivot, flapRotation) + flap.pivot + flap.segmentOffset;
    center = rotateByQuaternion(rotated - .5, orientation) + flap.instancePos + .5;
}
