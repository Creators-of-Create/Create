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
    vec4 data;// <float feather, float fade, float strength, float hsv marker>
    mat4 colorOp;
};

#define N 256
layout (std140) uniform Filters {
    int uCount;
    SphereFilter uSpheres[N];
};

float linearizeDepth(float d, float zNear, float zFar) {
    float clipZ = 2.0 * d - 1.0;
    return zNear * zFar / (zFar + zNear - clipZ * (zFar - zNear));
}

vec3 filterColor(mat4 colorOp, vec3 color) {
    // preserve alpha while transforming color
    vec4 i = vec4(color, 1.);
    i *= colorOp;
    return i.rgb;
}

float getDepth() {
    float depth = texture2D(uDepth, ScreenCoord).r;

    return linearizeDepth(depth, uNearPlane, uFarPlane);
}

float overlayFilterAmount(in vec3 worldPos, in vec4 sphere, in float feather) {
    float distance = distance(sphere.xyz, worldPos);
    return 1 - smoothstep(sphere.w, sphere.w + feather, distance);
}

float sphereFilterAmount(in vec3 worldDir, in float depth, in vec4 sphere, in vec4 data) {
    float feathering =  1 - smoothstep(sphere.w + data.x, sphere.w + data.x + data.y, length(sphere.xyz));
    feathering += overlayFilterAmount(worldDir * depth, sphere, data.x);
    vec3 oc = -sphere.xyz;

    float rayLengthSqr = dot(worldDir, worldDir);
    float b = 2.0 * dot(-sphere.xyz, worldDir);
    float sphereDistSqr = dot(sphere.xyz, sphere.xyz);
    float b2 = b*b;
    float d = 4. * rayLengthSqr;
    float e = 1. / (2.0*rayLengthSqr);

    float radius = sphere.w + data.x;
    float c = sphereDistSqr - radius*radius;
    float discriminant = b2 - d * c;
    float hitDepth = (-b - sqrt(discriminant)) * e;


    if (discriminant > 0 && hitDepth > 0 && hitDepth < depth) {
        //        float c = sphereDistSqr - sphere.w*sphere.w;
        //        float discriminant = b2 - d * c;
        //        float hitDepth = (-b - sqrt(discriminant)) * e;

        vec3 hitPos = worldDir * hitDepth;

        vec3 normal = normalize(hitPos - sphere.xyz);
        float normalDot = dot(normal, normalize(worldDir));
        return feathering + normalDot * normalDot;
    } else {
        return feathering;
    }
}

vec3 applyFilters(in vec3 worldDir, in float depth, in vec3 diffuse) {
    vec3 worldPos = worldDir * depth;

    vec3 accum = diffuse;
    vec3 diffuseHSV = rgb2hsv(accum);

    for (int i = 0; i < uCount; i++) {
        SphereFilter s = uSpheres[i];

        //float strength = overlayFilterAmount(worldPos, s.sphere, s.data.x);
        float strength = sphereFilterAmount(worldDir, depth, s.sphere, s.data);

        //accum = vec3(strength, strength, strength);

        vec3 toFilter = mix(diffuse, diffuseHSV, s.data.w);

        vec3 filtered = filterColor(s.colorOp, diffuse);

        filtered = mix(filtered, hsv2rgbWrapped(filtered), s.data.w);

        accum = mix(accum, filtered, clamp(strength * s.data.z, 0., 1.));
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

    vec4 diffuse = texture2D(uColor, ScreenCoord);

    Color = vec4(applyFilters(WorldDir, depth, diffuse.rgb), diffuse.a);
    //Color = debugGrid(WorldDir * depth, Color);
}
