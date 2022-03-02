#use "flywheel:context/fog.glsl"

uniform sampler3D uLightVolume;

uniform sampler2D uBlockAtlas;
uniform sampler2D uLightMap;

uniform vec3 uLightBoxSize;
uniform vec3 uLightBoxMin;
uniform mat4 uModel;

uniform float uTime;
uniform mat4 uViewProjection;
uniform vec3 uCameraPos;

#if defined(VERTEX_SHADER)

out vec3 BoxCoord;

vec4 FLWVertex(inout Vertex v) {
    vec4 worldPos = uModel * vec4(v.pos, 1.);

    BoxCoord = (worldPos.xyz - uLightBoxMin) / uLightBoxSize;

    FragDistance = max(length(worldPos.xz), abs(worldPos.y)); // cylindrical fog

    mat3 m;
    m[0] = uModel[0].xyz;
    m[1] = uModel[1].xyz;
    m[2] = uModel[2].xyz;
    v.normal = m * v.normal;

    v.pos = worldPos.xyz;
    return uViewProjection * worldPos;
}

#elif defined(FRAGMENT_SHADER)
#use "flywheel:core/lightutil.glsl"

// optimize discard usage
#if defined(ALPHA_DISCARD)
#if defined(GL_ARB_conservative_depth)
layout (depth_greater) out float gl_FragDepth;
#endif
#endif

in vec3 BoxCoord;

out vec4 FragColor;

vec4 FLWBlockTexture(vec2 texCoords) {
    return texture(uBlockAtlas, texCoords);
}

void FLWFinalizeColor(vec4 color) {
    float a = color.a;
    float fog = clamp(FLWFogFactor(), 0., 1.);

    color = mix(uFogColor, color, fog);
    color.a = a;

    #if defined(ALPHA_DISCARD)
    if (color.a < ALPHA_DISCARD) {
        discard;
    }
    #endif

    FragColor = color;
}

vec4 FLWLight(vec2 lightCoords) {
    lightCoords = max(lightCoords, texture(uLightVolume, BoxCoord).rg);

    return texture(uLightMap, shiftLight(lightCoords));
}

#endif
