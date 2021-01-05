#version 330 core
#define PI 3.1415926538

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

layout (location = 3) in vec3 instancePos;
layout (location = 4) in mat4 model;
layout (location = 8) in vec2 light;
layout (location = 9) in float speed;

out vec2 TexCoords;
out vec2 Light;

uniform float time;
uniform int ticks;
uniform mat4 projection;
uniform mat4 view;


void main() {
//    float textureIndex = fract((speed * time / 36 + cycle[1]) / cycle[0]) * cycle[0];
//    if (textureIndex < 0) {
//        textureIndex += cycle[0];
//    }
//
//    vec2 scrollPos = vec2(fract(textureIndex / 4), floor(textureIndex / 16));

    vec4 renderPos = model * vec4(aPos - vec3(0.5), 1f);
    renderPos += vec4(instancePos + vec3(0.5), 0);

    TexCoords = aTexCoords;
    gl_Position = projection * view * renderPos;
}
