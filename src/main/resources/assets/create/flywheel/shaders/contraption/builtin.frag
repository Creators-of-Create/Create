#flwinclude <"create:std/fog.glsl">

varying vec3 BoxCoord;
uniform sampler3D uLightVolume;

void FLWFinalizeColor(inout vec4 color) {
    #if defined(USE_FOG)
    float a = color.a;
    float fog = clamp(FLWFogFactor(), 0., 1.);

    color = mix(uFogColor, color, fog);
    color.a = a;
    #endif
}

vec4 FLWLight(vec2 lightCoords, sampler2D lightMap) {
    vec2 lm = max(lightCoords, texture3D(uLightVolume, BoxCoord).rg);
    return texture2D(lightMap, lm * 0.9375 + 0.03125);
}
