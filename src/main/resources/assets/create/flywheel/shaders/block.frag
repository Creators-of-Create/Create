#version 110

#flwbuiltins

varying vec2 TexCoords;
varying vec2 Light;
varying float Diffuse;
varying vec4 Color;

void main() {
    vec4 tex = FLWBlockTexture(TexCoords);

    vec4 color = vec4(tex.rgb * FLWLight(Light).rgb * Diffuse, tex.a) * Color;

    FLWFinalizeColor(color);

    gl_FragColor = color;
}
