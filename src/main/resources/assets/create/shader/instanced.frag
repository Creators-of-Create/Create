#version 330 core

in vec2 TexCoords;
in vec2 Light;
in float Diffuse;
in vec4 Color;
in float FragDistance;

out vec4 fragColor;

uniform vec2 uFogRange;
uniform vec4 uFogColor;

uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;

vec4 light() {
    vec2 lm = Light * 0.9375 + 0.03125;
    return texture2D(uLightMap, lm);
}

void main() {
    vec4 tex = texture2D(uBlockAtlas, TexCoords);

    vec4 color = vec4(tex.rgb * light().rgb * Diffuse, tex.a) * Color;

    float fog = (uFogRange.y - FragDistance) / (uFogRange.y - uFogRange.x);
    fog = clamp(fog, 0, 1);

    fragColor = mix(uFogColor, color, fog);
    fragColor.a = color.a;
}