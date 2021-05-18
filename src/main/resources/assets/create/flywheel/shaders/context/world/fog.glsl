#if defined(USE_FOG)
varying float FragDistance;
uniform vec4 uFogColor;
#endif

#if defined(USE_FOG_LINEAR)
uniform vec2 uFogRange;

float FLWFogFactor() {
    return (uFogRange.y - FragDistance) / (uFogRange.y - uFogRange.x);
}
    #endif

    #if defined(USE_FOG_EXP2)
uniform float uFogDensity;

float FLWFogFactor() {
    float dist = FragDistance * uFogDensity;
    return 1. / exp2(dist * dist);
}
    #endif
