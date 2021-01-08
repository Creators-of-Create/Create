#version 440 core

in vec3 Normal;
in vec2 TexCoords;
in vec2 Light;

out vec4 fragColor;

layout(binding=0) uniform sampler2D BlockAtlas;
layout(binding=1) uniform sampler2D LightMap;

float diffuse() {
    float x = Normal.x;
    float y = Normal.y;
    float z = Normal.z;
    return min(x * x * 0.6f + y * y * ((3f + y) / 4f) + z * z * 0.8f, 1f);
}

vec4 light() {
    vec2 lm = Light * 0.9375 + 0.03125;
    return texture2D(LightMap, lm);
}


void main() {
    vec4 tex = texture2D(BlockAtlas, TexCoords);

    tex *= vec4(light().rgb, 1);

    float df = diffuse();
    tex *= vec4(df, df, df, 1);

    fragColor = tex;
}