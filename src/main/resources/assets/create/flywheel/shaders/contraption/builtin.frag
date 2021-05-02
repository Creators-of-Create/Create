varying vec3 BoxCoord;
uniform sampler3D uLightVolume;

vec4 FLWLight(vec2 lightCoords, sampler2D lightMap) {
    vec2 lm = max(lightCoords, texture3D(uLightVolume, BoxCoord).rg);
    return texture2D(lightMap, lm * 0.9375 + 0.03125);
}
