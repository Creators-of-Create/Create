#version 330 core

in vec2 TexCoords;
in vec2 Light;
in float Diffuse;
in vec4 Color;

out vec4 fragColor;

uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;

vec4 light() {
    vec2 lm = Light * 0.9375 + 0.03125;
    return texture2D(uLightMap, lm);
}

void main() {
    vec4 tex = texture2D(uBlockAtlas, TexCoords);

    fragColor = vec4(tex.rgb * light().rgb * Diffuse, tex.a) * Color;
}