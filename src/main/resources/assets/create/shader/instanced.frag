#version 440 core
in vec2 TexCoords;
in vec2 Light;

out vec4 fragColor;

layout(binding=0) uniform sampler2D BlockAtlas;
layout(binding=1) uniform sampler2D LightMap;

float blendDarken(float base, float blend) {
    return min(blend,base);
}

vec3 blendDarken(vec3 base, vec3 blend) {
    return vec3(blendDarken(base.r,blend.r),blendDarken(base.g,blend.g),blendDarken(base.b,blend.b));
}

vec3 blendDarken(vec3 base, vec3 blend, float opacity) {
    return (blendDarken(base, blend) * opacity + base * (1.0 - opacity));
}

void main()
{
    vec4 tex = texture2D(BlockAtlas, TexCoords);

    vec4 light = texture2D(LightMap, Light);

    fragColor = vec4(blendDarken(tex.rgb, light.rgb, light.a), tex.a);
}