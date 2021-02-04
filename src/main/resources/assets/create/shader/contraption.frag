#version 330 core

in vec2 TexCoords;
in vec4 Color;
in float Diffuse;
in vec2 Light;

in vec3 BoxCoord;

out vec4 fragColor;

uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;
uniform sampler3D uLightVolume;

vec4 light() {
    vec2 lm = texture(uLightVolume, BoxCoord).rg * 0.9375 + 0.03125;
    return texture2D(uLightMap, max(lm, Light));
}

void main() {
    vec4 tex = texture2D(uBlockAtlas, TexCoords);

    fragColor = vec4(tex.rgb * light().rgb * Diffuse * Color.rgb, tex.a);
}