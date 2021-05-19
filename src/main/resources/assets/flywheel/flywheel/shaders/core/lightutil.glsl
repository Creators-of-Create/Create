// Adjust the [0,1] normalized lightmap value based on the texture matrix from LightTexture#enableLightmap
vec2 shiftLight(vec2 lm) {
    return lm * 0.99609375 + 0.03125;// * 255/256 + 1/32
}
