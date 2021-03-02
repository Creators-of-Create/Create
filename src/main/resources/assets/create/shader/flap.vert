#version 110
#define PI 3.1415926538

attribute vec3 aPos;
attribute vec3 aNormal;
attribute vec2 aTexCoords;

attribute vec3 aInstancePos;
attribute vec2 aLight;

attribute vec3 aSegmentOffset;
attribute vec3 aPivot;
attribute float aHorizontalAngle;
attribute float aIntensity;
attribute float aFlapScale;

attribute float aFlapness;

// outputs
varying vec2 TexCoords;
varying vec4 Color;
varying float Diffuse;
varying vec2 Light;

#if defined(CONTRAPTION)
varying vec3 BoxCoord;

uniform vec3 uLightBoxSize;
uniform vec3 uLightBoxMin;
uniform mat4 uModel;
#endif

uniform float uTime;
uniform mat4 uViewProjection;
uniform int uDebug;

uniform vec3 uCameraPos;
varying float FragDistance;

float diffuse(vec3 normal) {
    float x = normal.x;
    float y = normal.y;
    float z = normal.z;
    return min(x * x * .6 + y * y * ((3. + y) / 4.) + z * z * .8, 1.);
}

mat4 rotate(vec3 axis, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1. - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.,
    oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.,
    oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.,
    0.,                                 0.,                                 0.,                                 1.);
}

float toRad(float degrees) {
    return fract(degrees / 360.) * PI * 2.;
}

float getFlapAngle() {
    float absFlap = abs(aFlapness);

    float angle = sin((1. - absFlap) * PI * aIntensity) * 30. * aFlapness * aFlapScale;

    float halfAngle = angle * 0.5;

    float which = step(0., aFlapness);
    float degrees = which * halfAngle + (1. - which) * angle; // branchless conditional multiply

    return -toRad(degrees);
}

void main() {
    float flapAngle = getFlapAngle();

    mat4 orientation = rotate(vec3(0., 1., 0.), toRad(aHorizontalAngle));
    mat4 flapRotation = rotate(vec3(1., 0., 0.), flapAngle);

    vec4 worldPos = flapRotation * vec4(aPos - aPivot, 1.) + vec4(aPivot + aSegmentOffset, 0.);
    worldPos = orientation * vec4(worldPos.xyz - .5, 1.) + vec4(aInstancePos + .5, 0.);

    #ifdef CONTRAPTION
    worldPos = uModel * worldPos;
    mat4 normalMat = uModel * orientation * flapRotation;

    BoxCoord = (worldPos.xyz - uLightBoxMin) / uLightBoxSize;
    FragDistance = length(worldPos.xyz);
    #else
    mat4 normalMat = orientation * flapRotation;

    FragDistance = length(worldPos.xyz - uCameraPos);
    #endif

    vec3 norm = normalize(normalMat * vec4(aNormal, 0.)).xyz;

    Diffuse = diffuse(norm);
    TexCoords = aTexCoords;
    Light = aLight;
    gl_Position = uViewProjection * worldPos;

    Color = vec4(1.);
}
