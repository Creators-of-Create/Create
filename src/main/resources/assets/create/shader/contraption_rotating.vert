#version 330 core
#define PI 3.1415926538
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

layout (location = 3) in vec3 instancePos;
layout (location = 4) in vec2 light;
layout (location = 5) in float speed;
layout (location = 6) in float rotationOffset;
layout (location = 7) in vec3 rotationAxis;

out float Diffuse;
out vec2 TexCoords;
out vec4 Color;
out vec3 BoxCoord;

uniform vec3 lightBoxSize;
uniform vec3 lightBoxMin;
uniform vec3 cPos;
uniform vec3 cRot;

uniform float time;
uniform int ticks;
uniform mat4 projection;
uniform mat4 view;

mat4 rotate(vec3 axis, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.,
    oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.,
    oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.,
    0.,                                 0.,                                 0.,                                 1.);
}

mat4 kineticRotation() {
    float degrees = rotationOffset + time * speed * -3./10.;
    float angle = fract(degrees / 360.) * PI * 2.;

    return rotate(normalize(rotationAxis), angle);
}

mat4 contraptionRotation() {
    vec3 rot = -fract(cRot / 360) * PI * 2;
    return rotate(vec3(0, 1, 0), rot.y) * rotate(vec3(0, 0, 1), rot.z) * rotate(vec3(1, 0, 0), rot.x);
}

float diffuse(vec3 normal) {
    float x = normal.x;
    float y = normal.y;
    float z = normal.z;
    return min(x * x * 0.6f + y * y * ((3f + y) / 4f) + z * z * 0.8f, 1f);
}

void main() {
    mat4 kineticRotation = kineticRotation();
    vec4 localPos = kineticRotation * vec4(aPos - 0.5, 1f) + vec4(instancePos, 0);

    mat4 contraptionRotation = contraptionRotation();
    vec4 worldPos = contraptionRotation * localPos + vec4(cPos + 0.5, 0);

    BoxCoord = (worldPos.xyz - lightBoxMin) / lightBoxSize;
    Diffuse = diffuse(normalize(contraptionRotation * kineticRotation * vec4(aNormal, 0.)).xyz);
    Color = vec4(1.);
    TexCoords = aTexCoords;
    gl_Position = projection * view * worldPos;
}