#version 110

varying vec2 TexCoords;
varying vec2 Light;
varying float Diffuse;
varying vec4 Color;

uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;

#if defined(USE_FOG)
varying float FragDistance;
uniform vec4 uFogColor;
#endif

#if defined(USE_FOG_LINEAR)
uniform vec2 uFogRange;

float fogFactor() {
    return (uFogRange.y - FragDistance) / (uFogRange.y - uFogRange.x);
}
#endif

#ifdef USE_FOG_EXP2
uniform float uFogDensity;

float fogFactor() {
    float dist = FragDistance * uFogDensity;
    return 1. / exp2(dist * dist);
}
    #endif

    #ifdef CONTRAPTION
    #flwinclude <"create:contraption/builtin.frag">
    #else
    #flwinclude <"create:std/builtin.frag">
    #endif

void main() {
    vec4 tex = texture2D(uBlockAtlas, TexCoords);

    vec4 color = vec4(tex.rgb * FLWLight(Light, uLightMap).rgb * Diffuse, tex.a) * Color;

    #if defined(USE_FOG)
    float fog = clamp(fogFactor(), 0., 1.);

    gl_FragColor = mix(uFogColor, color, fog);
    gl_FragColor.a = color.a;
#else
    gl_FragColor = color;
#endif
}
