#version 110
#define PI 3.1415926538
attribute vec3 aPos;
attribute vec3 aNormal;
attribute vec2 aTexCoords;

attribute vec3 aInstancePos;
attribute vec2 aLight;
attribute vec3 aNetworkTint;
attribute float aSpeed;
attribute float aOffset;
attribute vec3 aAxis;

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

mat4 rotate(vec3 axis, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1. - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.,
                0.,                                 0.,                                 0.,                                 1.);
}

float diffuse(vec3 normal) {
    float x = normal.x;
    float y = normal.y;
    float z = normal.z;
    return min(x * x * .6 + y * y * ((3. + y) / 4.) + z * z * .8, 1.);
}

mat4 rotation(vec3 rot) {
    return rotate(vec3(0., 1., 0.), rot.y) * rotate(vec3(0., 0., 1.), rot.z) * rotate(vec3(1., 0., 0.), rot.x);
}

mat4 kineticRotation() {
    float degrees = aOffset + uTime * aSpeed * -3./10.;
    float angle = fract(degrees / 360.) * PI * 2.;

    return rotate(aAxis, angle);
}

void main() {
    mat4 kineticRotation = kineticRotation();
    vec4 worldPos = kineticRotation * vec4(aPos - .5, 1.) + vec4(aInstancePos + .5, 0.);

    #ifdef CONTRAPTION
    worldPos = uModel * worldPos;
    mat4 normalMat = uModel * kineticRotation;

    BoxCoord = (worldPos.xyz - uLightBoxMin) / uLightBoxSize;
    FragDistance = length(worldPos.xyz);
    #else
    mat4 normalMat = kineticRotation;

    FragDistance = length(worldPos.xyz - uCameraPos);
    #endif

    vec3 norm = normalize(normalMat * vec4(aNormal, 0.)).xyz;

    Diffuse = diffuse(norm);
    TexCoords = aTexCoords;
    Light = aLight;
    gl_Position = uViewProjection * worldPos;

    #ifdef CONTRAPTION
    if (uDebug == 2) {
        Color = vec4(norm, 1.);
    } else {
        Color = vec4(1.);
    }
    #else
    if (uDebug == 1) {
        Color = vec4(aNetworkTint, 1.);
    } else if (uDebug == 2) {
        Color = vec4(norm, 1.);
    } else {
        Color = vec4(1.);
    }
    #endif
}