#flwinclude <"create:context/std/fog.glsl">

varying vec3 BoxCoord;
uniform sampler3D uLightVolume;

uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;

vec4 FLWBlockTexture(vec2 texCoords) {
    return texture2D(uBlockAtlas, texCoords);
}

void FLWFinalizeColor(vec4 color) {
    #if defined(USE_FOG)
    float a = color.a;
    float fog = clamp(FLWFogFactor(), 0., 1.);

    color = mix(uFogColor, color, fog);
    color.a = a;
    #endif

    gl_FragColor = color;
}

vec4 FLWLight(vec2 lightCoords) {
    vec2 lm = max(lightCoords, texture3D(uLightVolume, BoxCoord).rg);
    return texture2D(uLightMap, lm * 0.99609375 + 0.03125); // * 255/256 + 1/32
}
