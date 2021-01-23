#version 440 core
#define PI 3.1415926538

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;
layout (location = 3) in vec4 aColor;

out float Diffuse;
out vec2 TexCoords;
out vec4 Color;
out vec3 BoxCoord;

uniform vec3 lightBoxSize;
uniform vec3 lightBoxMin;
uniform mat4 model;

uniform float time;
uniform int ticks;
uniform mat4 projection;
uniform mat4 view;
uniform int debug;

mat4 rotate(vec3 axis, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1 - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0,
                0,                                  0,                                  0,                                  1);
}

float diffuse(vec3 normal) {
    float x = normal.x;
    float y = normal.y;
    float z = normal.z;
    return min(x * x * .6 + y * y * ((3 + y) / 4) + z * z * .8, 1);
}

void main() {
    vec4 worldPos = model * vec4(aPos, 1);

    vec3 norm = (model * vec4(aNormal, 0)).xyz;

    BoxCoord = (worldPos.xyz - lightBoxMin) / lightBoxSize;
    Diffuse = diffuse(norm);
    Color = aColor / diffuse(aNormal);
    TexCoords = aTexCoords;
    gl_Position = projection * view * worldPos;

    if (debug == 2) {
        Color = vec4(norm, 1);
    } else {
        Color = aColor / diffuse(aNormal);
    }
}
