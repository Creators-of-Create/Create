#version 330 core
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

out vec2 TexCoords;
out vec2 Light;
out float Diffuse;
out vec4 Color;

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
    vec3 rot = fract(rotationDegrees / 360) * PI * 2;

    mat4 rotation = rotate(vec3(0, 1, 0), rot.y) * rotate(vec3(0, 0, 1), rot.z) * rotate(vec3(1, 0, 0), rot.x);

    vec4 renderPos = rotation * vec4(aPos - vec3(.5), 1);
    renderPos += vec4(instancePos + vec3(.5), 0);

    float scrollSize = scrollTexture.w - scrollTexture.y;

    float scroll = fract(speed * time / (36 * 16)) * scrollSize * scrollMult;

    vec3 norm = (rotation * vec4(aNormal, 0)).xyz;

    Diffuse = diffuse(norm);
    Light = light;
    TexCoords = aTexCoords - sourceUV + scrollTexture.xy + vec2(0, scroll);
    gl_Position = projection * view * renderPos;

    if (debug == 1) {
        Color = vec4(networkTint, 1);
    } else if (debug == 2) {
        Color = vec4(norm, 1);
    } else {
        Color = vec4(1);
    }
}
