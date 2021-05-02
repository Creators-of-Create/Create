vec4 FLWLight(vec2 lightCoords, sampler2D lightMap) {
    vec2 lm = lightCoords * 0.9375 + 0.03125;
    return texture2D(lightMap, lm);
}
