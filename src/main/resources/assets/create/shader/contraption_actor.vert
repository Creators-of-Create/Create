#version 330 core
#define PI 3.1415926538
// model data
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

// instance data
layout (location = 3) in vec3 instancePos;
layout (location = 4) in float rotationOffset;
layout (location = 5) in vec3 localRotationAxis;
layout (location = 6) in vec3 localRotation;
layout (location = 7) in vec3 rotationCenter;

// dynamic data
//layout (location = 7) in vec3 relativeMotion;

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
    float oc = 1.0 - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.,
    oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.,
    oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.,
    0.,                                 0.,                                 0.,                                 1.);
}

mat4 rotation(vec3 rot) {
    return rotate(vec3(0, 1, 0), rot.y) * rotate(vec3(0, 0, 1), rot.z) * rotate(vec3(1, 0, 0), rot.x);
}

mat4 kineticRotation() {
    const float speed = 20;
    float degrees = rotationOffset + time * speed * -3./10.;
    float angle = fract(degrees / 360.) * PI * 2.;

    return rotate(normalize(localRotationAxis), angle);
}

float diffuse(vec3 normal) {
    float x = normal.x;
    float y = normal.y;
    float z = normal.z;
    return min(x * x * 0.6f + y * y * ((3f + y) / 4f) + z * z * 0.8f, 1f);
}

void main() {
    vec3 rot = fract(localRotation / 360.) * PI * 2.;
    mat4 localRot = rotation(rot);
    vec4 localPos = localRot * vec4(aPos - 0.5, 1f) + vec4(0.5, 0.5, 0.5, 0);

    mat4 kineticRotation = kineticRotation();
    localPos = kineticRotation * vec4(localPos.xyz - rotationCenter, 1) + vec4(instancePos + rotationCenter, 0);

    vec4 worldPos = model * localPos;

    BoxCoord = (worldPos.xyz - lightBoxMin) / lightBoxSize;
    Diffuse = diffuse(normalize(model * localRot * kineticRotation * vec4(aNormal, 0.)).xyz);
    Color = vec4(1.);
    TexCoords = aTexCoords;
    gl_Position = projection * view * worldPos;
}