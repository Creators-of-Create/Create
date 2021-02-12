#version 110
#define PI 3.1415926538

attribute vec3 aPos;
attribute vec3 aNormal;
attribute vec2 aTexCoords;
attribute vec4 aColor;
attribute vec2 aModelLight;

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

void main() {
    vec4 worldPos = uModel * vec4(aPos, 1.);

    vec3 norm = (uModel * vec4(aNormal, 0.)).xyz;

    BoxCoord = (worldPos.xyz - uLightBoxMin) / uLightBoxSize;
    Diffuse = diffuse(norm);
    Color = aColor / diffuse(aNormal);
    TexCoords = aTexCoords;
    Light = aModelLight;
    FragDistance = length(worldPos.xyz - uCameraPos);
    gl_Position = uViewProjection * worldPos;

    if (uDebug == 2) {
        Color = vec4(norm, 1.);
    } else {
        Color = aColor / diffuse(aNormal);
    }
}
