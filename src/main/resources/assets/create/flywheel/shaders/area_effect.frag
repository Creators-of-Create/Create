#version 140

#flwinclude <"create:core/color.glsl">

in vec2 ScreenCoord;
in vec3 WorldDir;

out vec4 Color;

// constants
uniform sampler2D uDepth;
uniform sampler2D uColor;
uniform float uNearPlane = 0.15;
uniform float uFarPlane = 1.;
uniform vec3 uCameraPos;

struct SphereFilter {
    vec4 sphere;// <vec3 position, float radius>
    float feather;
    mat4 colorOp;
};

#define N 16
layout (std140) uniform Filters {
    int uCount;
    SphereFilter uSpheres[N];
};

float linearizeDepth(float d, float zNear, float zFar) {
    float clipZ = 2.0 * d - 1.0;
    return zNear * zFar / (zFar + zNear - clipZ * (zFar - zNear));
}

vec4 filterColor(mat4 colorOp, vec4 frag) {
    // preserve alpha while transforming color
    vec4 i = vec4(frag.rgb, 1.);
    i *= colorOp;
    return vec4(i.rgb, frag.a);
}

float getDepth() {
    float depth = texture2D(uDepth, ScreenCoord).r;

    return linearizeDepth(depth, uNearPlane, uFarPlane);
}

vec4 applyFilters(vec3 worldPos, vec4 diffuse) {
    vec4 accum = diffuse;

    for (int i = 0; i < uCount; i++) {
        SphereFilter s = uSpheres[i];

        float distance = distance(s.sphere.xyz, worldPos);
        float strength = 1 - smoothstep(s.sphere.w - s.feather, s.sphere.w + s.feather, distance);

        accum = mix(accum, filterColor(s.colorOp, diffuse), strength);
    }

    return accum;
}

vec4 debugGrid(vec3 worldPos, vec4 diffuse) {
    vec3 fractionalCoords = fract(worldPos - uCameraPos);

    vec3 isBonudary = step(15./16., fractionalCoords);

    return vec4(mix(diffuse.rgb, fractionalCoords, isBonudary), 1.);
}

void main() {
    float depth = getDepth();
    vec3 worldPos = WorldDir * depth;

    vec4 diffuse = texture2D(uColor, ScreenCoord);

    Color = applyFilters(worldPos, diffuse);
    //Color = debugGrid(worldPos, diffuse);
}
