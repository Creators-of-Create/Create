#version 110

varying vec2 TexCoords;
varying vec4 Color;
varying float Diffuse;
varying vec2 Light;
varying float FragDistance;

varying vec3 BoxCoord;

uniform vec2 uFogRange;
uniform vec4 uFogColor;

uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;
uniform sampler3D uLightVolume;

vec4 light() {
    vec2 lm = texture3D(uLightVolume, BoxCoord).rg * 0.9375 + 0.03125;
    return texture2D(uLightMap, max(lm, Light));
}

void main() {
    vec4 tex = texture2D(uBlockAtlas, TexCoords);

    vec4 color = vec4(tex.rgb * light().rgb * Diffuse * Color.rgb, tex.a);

    float fog = (uFogRange.y - FragDistance) / (uFogRange.y - uFogRange.x);
    fog = clamp(fog, 0., 1.);

    gl_FragColor = mix(uFogColor, color, fog);
    gl_FragColor.a = color.a;
}