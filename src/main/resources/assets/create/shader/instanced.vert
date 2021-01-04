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
layout (location = 8) in int[2] uvScroll; // uvScroll[0] <- cycleLength, uvScroll[1] <- cycleOffset

out vec2 TexCoords;
out vec2 Light;

uniform float time;
uniform int ticks;
uniform mat4 projection;
uniform mat4 view;

mat4 rotationMatrix(vec3 axis, float angle)
{
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.,
                0.,                                 0.,                                 0.,                                 1.);
}

void main()
{
    vec4 renderPos;
    int textureIndex = 0;
    if (abs(rotationAxis.x) + abs(rotationAxis.y) + abs(rotationAxis.z) < 0.2) {
        renderPos = vec4(aPos + instancePos, 1f);

        textureIndex = int((speed * time / 36) + uvScroll[1]) % uvScroll[0];
        if (textureIndex < 0) {
            textureIndex += uvScroll[0];
        }

    } else {
        float degrees = rotationOffset + time * speed * 3./10.;
        float angle = fract(-degrees / 360.) * PI * 2.;

        renderPos = rotationMatrix(rotationAxis, angle) * vec4(aPos - vec3(0.5), 1f);

        renderPos += vec4(instancePos + vec3(0.5), 0);
    }

    TexCoords = aTexCoords + vec2(float(textureIndex % 4) / 4f, float(textureIndex / 4) / 4f);

    gl_Position = projection * view * renderPos;
    Light = light;
}