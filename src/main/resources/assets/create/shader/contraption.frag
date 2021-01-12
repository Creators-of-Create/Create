#version 440 core

in vec2 TexCoords;
in vec4 Color;
in float Diffuse;
in vec3 BoxCoord;

out vec4 fragColor;

layout(binding=0) uniform sampler2D BlockAtlas;
layout(binding=1) uniform sampler2D LightMap;
layout(binding=4) uniform sampler3D LightVolume;

vec4 light() {
    vec2 lm = texture(LightVolume, BoxCoord).rg * 0.9375 + 0.03125;
    return texture2D(LightMap, lm);
}

void main() {
    vec4 tex = texture2D(BlockAtlas, TexCoords);

    fragColor = vec4(tex.rgb * light().rgb * Diffuse, tex.a) * Color;
}