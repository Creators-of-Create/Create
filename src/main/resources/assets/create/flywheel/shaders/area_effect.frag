#version 140

#flwinclude <"flywheel:core/color.glsl">

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
    vec4 d1;// <float feather, float fade, float density, float blend mode>
    vec4 strength;// <float surfaceStrength, float bubbleStrength, float strength, float invert>
    vec4 channelMask;// <vec3 rgb>
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

float surfaceFilterStrength(vec3 worldPos, vec4 sphere, float feather) {
    float distance = distance(sphere.xyz, worldPos);
    return 1 - smoothstep(sphere.w, sphere.w + feather, distance);
}

vec2 raySphere(vec3 worldDir, vec3 position, float radius) {
    float rayLengthSqr = dot(worldDir, worldDir);
    float sphereDistSqr = dot(position, position);

    const vec3 M = vec3(2., 2., 4.);
    vec3 f = M * vec3(dot(-position, worldDir), vec2(rayLengthSqr));

    vec2 s = vec2(f.x, radius);
    vec2 s2 = s * s;
    float c = sphereDistSqr - s2.y;
    float dc = f.z * c;

    float discriminant = s2.x - dc;
    float hitDepth = (-f.x - sqrt(discriminant)) / f.y;

    return vec2(discriminant, hitDepth);
}

// if i == 0 return s
// if i == 1 return 1 - s
float invert(float s, float i) {
    return i - 2*i*s + s;
}

float bubbleFilterStrength(vec3 worldDir, float depth, vec4 sphere, float feather, float density) {
    vec3 position = sphere.xyz;

    vec2 hit = raySphere(worldDir, position, sphere.w + feather);
    float hitDepth = hit.y;

    float strength = 0.;

    //float boo = step(0., discriminant) * step(0., hitDepth) * step(0., depth - hitDepth);
    if (hit.x > 0 && hitDepth > 0 && hitDepth < depth) {
        vec3 hitPos = worldDir * hitDepth;

        vec3 normal = normalize(hitPos - position);
        float normalDot = dot(normal, normalize(worldDir));
        // blend into the effect based on the distance between the fragcoord and point on the sphere
        // this avoinds having hard edges
        strength += mix(0., normalDot * normalDot * density, clamp(depth - hitDepth, 0., feather + 1.));
    }

    return clamp(strength, 0., 1.);// * boo;
}

float filterStrength(vec3 worldDir, float depth, inout SphereFilter f) {
    vec4 sphere = f.sphere;
    vec4 data = f.d1;
    float feather = data.x;

    float strength;
    // transition effect
    float transitionRadius = sphere.w + feather;
    strength = 1. - smoothstep(transitionRadius, transitionRadius + max(0.5, data.y), length(sphere.xyz));
    // bubble effect
    strength = max(strength, bubbleFilterStrength(worldDir, depth, sphere, feather, data.z));

    strength *= f.strength.y;
    // surface effect
    strength = max(strength, surfaceFilterStrength(worldDir * depth, sphere, feather) * f.strength.x);

    return strength * f.strength.z;
}

vec3 applyFilters(vec3 worldDir, float depth, vec3 diffuse) {
    vec3 worldPos = worldDir * depth;

    vec3 accum = vec3(diffuse);

    for (int i = 0; i < uCount; i++) {
        SphereFilter s = uSpheres[i];

        float strength = filterStrength(worldDir, depth, s);

        strength = invert(strength, s.strength.w);

        if (strength > 0) {
            const float fcon = 0.;

            vec3 baseColor = mix(diffuse, accum, s.d1.w);

            vec3 filtered = filterColor(s.colorOp, baseColor);

            //            vec3 baseHsv = rgb2hsv(baseColor);
            //            vec3 maskHsv = rgb2hsv(s.colorMask.rgb);
            //            float diff = dot(abs(baseHsv - maskHsv), vec3(1., 1.1, 0.1));
            //            float colorMask = step(s.colorMask.w, diff);
            float mixing = clamp(strength, 0., 1.);

            accum = mix(accum, filtered, mixing * s.channelMask.xyz);
            //accum = vec3(colorMask);
        }
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
