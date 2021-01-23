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

mat4 rotation(vec3 rot) {
    return rotate(vec3(0, 1, 0), rot.y) * rotate(vec3(0, 0, 1), rot.z) * rotate(vec3(1, 0, 0), rot.x);
}

mat4 kineticRotation() {
    const float speed = -20;
    float degrees = rotationOffset + time * speed * -3/10;
    float angle = fract(degrees / 360) * PI * 2;

    return rotate(normalize(localRotationAxis), angle);
}

void main() {
    mat4 kineticRotation = kineticRotation();
    vec4 localPos = kineticRotation * vec4(aPos - rotationCenter, 1) + vec4(rotationCenter, 0);
    //localPos = vec4(localPos.xyz + instancePos, 1);

    vec3 rot = fract(localRotation / 360) * PI * 2;
    mat4 localRot = rotation(rot);
    localPos = localRot * vec4(localPos.xyz - .5, 1) + vec4(instancePos + .5, 0);

    vec4 worldPos = model * localPos;

    vec3 norm = normalize(model * localRot * kineticRotation * vec4(aNormal, 0)).xyz;

    BoxCoord = (worldPos.xyz - lightBoxMin) / lightBoxSize;
    Diffuse = diffuse(norm);
    TexCoords = aTexCoords;
    gl_Position = projection * view * worldPos;

    if (debug == 2) {
        Color = vec4(norm, 1);
    } else {
        Color = vec4(1);
    }
}