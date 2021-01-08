#version 440 core

in vec2 TexCoords;
in vec2 Light;
in vec4 Color;
in float Diffuse;

out vec4 fragColor;

layout(binding=0) uniform sampler2D BlockAtlas;
layout(binding=1) uniform sampler2D LightMap;

vec4 light() {
    vec2 lm = Light * 0.9375 + 0.03125;
    return texture2D(LightMap, lm);
}


void main() {
    vec4 tex = texture2D(BlockAtlas, TexCoords);

    tex *= vec4(light().rgb * Diffuse, 1);

    fragColor = tex;
}