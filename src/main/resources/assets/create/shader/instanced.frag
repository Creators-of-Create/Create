#version 110

varying vec2 TexCoords;
varying vec2 Light;
varying float Diffuse;
varying vec4 Color;
varying float FragDistance;

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
    fog = clamp(fog, 0., 1.);

    gl_FragColor = mix(uFogColor, color, fog);
    gl_FragColor.a = color.a;
}