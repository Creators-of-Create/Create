#version 110

#flwbuiltins

varying vec2 TexCoords;
varying vec2 Light;
varying float Diffuse;
varying vec4 Color;

uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;

void main() {
    vec4 tex = texture2D(uBlockAtlas, TexCoords);

    vec4 color = vec4(tex.rgb * FLWLight(Light, uLightMap).rgb * Diffuse, tex.a) * Color;

    FLWFinalizeColor(color);

    gl_FragColor = color;
}
