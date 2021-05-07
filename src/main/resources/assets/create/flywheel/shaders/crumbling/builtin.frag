#flwinclude <"create:std/fog.glsl">

uniform vec2 uTextureScale;
uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;

vec4 FLWBlockTexture(vec2 texCoords) {
    return texture2D(uBlockAtlas, texCoords * uTextureScale);
}

void FLWFinalizeColor(inout vec4 color) {
    #if defined(USE_FOG)
    float a = color.a;
    float fog = clamp(FLWFogFactor(), 0., 1.);

    color = mix(uFogColor, color, fog);
    color.a = a;
    #endif
}

vec4 FLWLight(vec2 lightCoords) {
    return vec4(1.);
}
