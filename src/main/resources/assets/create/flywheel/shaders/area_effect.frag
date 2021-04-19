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
    vec4 data;// <float feather, float fade, float density, float strength>
    mat4 colorOp;
};

vec3 getPosition(SphereFilter f) {
    return f.sphere.xyz;
}

float getRadius(SphereFilter f) {
    return f.sphere.w;
}

float getFeather(SphereFilter f) {
    return f.data.x;
}

float getFade(SphereFilter f) {
    return f.data.y;
}

float getDensity(SphereFilter f) {
    return f.data.z;
}

float getStrength(SphereFilter f) {
    return f.data.w;
}

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

float surfaceFilterStrength(vec3 worldPos, vec4 sphere, float feather) {
    float distance = distance(sphere.xyz, worldPos);
    return 1 - smoothstep(sphere.w, sphere.w + feather, distance);
}

float bubbleFilterStrength(vec3 worldDir, float depth, vec4 sphere, float feather, float density) {
    vec3 position = sphere.xyz;

    float rayLengthSqr = dot(worldDir, worldDir);
    float b = 2.0 * dot(-position, worldDir);
    float sphereDistSqr = dot(position, position);
    float b2 = b*b;
    float d = 4. * rayLengthSqr;
    float e = 1. / (2.0*rayLengthSqr);

    float radius = sphere.w + feather;
    float c = sphereDistSqr - radius*radius;
    float discriminant = b2 - d * c;
    float hitDepth = (-b - sqrt(discriminant)) * e;

    float strength = 0.;
    if (discriminant > 0 && hitDepth > 0 && hitDepth < depth) {
        vec3 hitPos = worldDir * hitDepth;

        vec3 normal = normalize(hitPos - position);
        float normalDot = dot(normal, normalize(worldDir));
        // blend into the effect based on the distance between the fragcoord and point on the sphere
        // this avoinds having hard edges
        strength += mix(0., normalDot * normalDot * density, clamp(depth - hitDepth, 0., feather + 1.));
    }

    return clamp(strength, 0., 1.);
}

float filterStrength(vec3 worldDir, float depth, vec4 sphere, vec4 data) {
    float feather = data.x;

    float strength = 0.;
    // transition effect
    float transitionRadius = sphere.w + feather;
    strength += 1. - smoothstep(transitionRadius, transitionRadius + data.y, length(sphere.xyz));
    // surface effect
    strength += surfaceFilterStrength(worldDir * depth, sphere, feather);
    // bubble effect
    strength += bubbleFilterStrength(worldDir, depth, sphere, feather, data.z);

    return strength;
}

vec3 applyFilters(vec3 worldDir, float depth, vec3 diffuse) {
    vec3 worldPos = worldDir * depth;

    vec3 accum = vec3(diffuse);

    for (int i = 0; i < uCount; i++) {
        SphereFilter s = uSpheres[i];

        float strength = filterStrength(worldDir, depth, s.sphere, s.data);

        vec3 filtered = filterColor(s.colorOp, diffuse);

        accum = mix(accum, filtered, clamp(strength * s.data.w, 0., 1.));
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
