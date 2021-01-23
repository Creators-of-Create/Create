#version 420 core
#define PI 3.1415926538

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

layout (location = 3) in vec3 networkTint;
layout (location = 4) in vec3 instancePos;
layout (location = 5) in vec2 light;
layout (location = 6) in vec3 rotationDegrees;
layout (location = 7) in float speed;
layout (location = 8) in vec2 sourceUV;
layout (location = 9) in vec4 scrollTexture;
layout (location = 10) in float scrollMult;

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

mat4 localRotation() {
    vec3 rot = fract(rotationDegrees / 360) * PI * 2;
    return rotation(rot);
}

void main() {
    mat4 localRotation = localRotation();
    vec4 localPos = localRotation * vec4(aPos - .5, 1) + vec4(instancePos + .5, 0);

    vec4 worldPos = model * localPos;

    float scrollSize = scrollTexture.w - scrollTexture.y;
    float scroll = fract(speed * time / (36 * 16)) * scrollSize * scrollMult;

    vec3 norm = normalize(model * localRotation * vec4(aNormal, 0)).xyz;

    BoxCoord = (worldPos.xyz - lightBoxMin) / lightBoxSize;
    Diffuse = diffuse(norm);
    TexCoords = aTexCoords - sourceUV + scrollTexture.xy + vec2(0, scroll);
    gl_Position = projection * view * worldPos;

    if (debug == 2) {
        Color = vec4(norm, 1);
    } else {
        Color = vec4(1);
    }
}
