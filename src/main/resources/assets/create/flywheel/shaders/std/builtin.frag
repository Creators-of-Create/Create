#flwinclude <"create:std/fog.glsl">

void FLWFinalizeColor(vec4 color) {
    #if defined(USE_FOG)
    float a = color.a;
    float fog = clamp(FLWFogFactor(), 0., 1.);

    color = mix(uFogColor, color, fog);
    color.a = a;
    #endif
}

vec4 FLWLight(vec2 lightCoords, sampler2D lightMap) {
    vec2 lm = lightCoords * 0.9375 + 0.03125;
    return texture2D(lightMap, lm);
}
