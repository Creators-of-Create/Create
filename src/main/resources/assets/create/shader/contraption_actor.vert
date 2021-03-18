#version 110
#define PI 3.1415926538
// model data
attribute vec3 aPos;
attribute vec3 aNormal;
attribute vec2 aTexCoords;

// instance data
attribute vec3 aInstancePos;
attribute vec2 aModelLight;
attribute float aOffset;
attribute vec3 aAxis;
attribute vec3 aInstanceRot;
attribute vec3 aRotationCenter;
attribute float aSpeed;


varying float Diffuse;
varying vec2 TexCoords;
varying vec4 Color;
varying vec3 BoxCoord;
varying vec2 Light;

uniform vec3 uLightBoxSize;
uniform vec3 uLightBoxMin;
uniform mat4 uModel;

uniform float uTime;
uniform mat4 uViewProjection;
uniform int uDebug;

uniform vec3 uCameraPos;

#if defined(USE_FOG)
varying float FragDistance;
#endif

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
    float degrees = aOffset + uTime * aSpeed / 20.;
    float angle = fract(degrees / 360.) * PI * 2.;

    return rotate(normalize(aAxis), -angle);
}

void main() {
    mat4 kineticRotation = kineticRotation();
    vec4 localPos = kineticRotation * vec4(aPos - aRotationCenter, 1.) + vec4(aRotationCenter, 0.);

    vec3 rot = fract(aInstanceRot / 360.) * PI * 2.;
    mat4 localRot = rotation(rot);
    localPos = localRot * vec4(localPos.xyz - .5, 1.) + vec4(aInstancePos + .5, 0.);

    vec4 worldPos = uModel * localPos;

    vec3 norm = normalize(uModel * localRot * kineticRotation * vec4(aNormal, 0.)).xyz;

    BoxCoord = (worldPos.xyz - uLightBoxMin) / uLightBoxSize;
    Diffuse = diffuse(norm);
    TexCoords = aTexCoords;
    Light = aModelLight;
    gl_Position = uViewProjection * worldPos;

    #if defined(USE_FOG)
    FragDistance = length(worldPos.xyz);
    #endif

    if (uDebug == 2) {
        Color = vec4(norm, 1.);
    } else {
        Color = vec4(1.);
    }
}