#version 120

varying vec4 Vertex;
varying vec3 CameraDir;

//layout (std140) struct Sphere {
//    vec4 positionRadius;
//    vec4 color;
//} uSphere;

uniform sampler2D uDepth;
uniform sampler2D uColor;
uniform mat4 uInverseProjection;
uniform mat4 uInverseView;

uniform float uNearPlane = 0.15;
uniform float uFarPlane = 1;
uniform vec3 uSphereCenter = vec3(0, 0, 0);
uniform float uSphereRadius = 1;
uniform float uSphereFeather = 0.05;

float linearizeDepth(float d, float zNear, float zFar) {
    float z_n = 2.0 * d - 1.0;
    return 2.0 * zNear * zFar / (zFar + zNear - z_n * (zFar - zNear));
}

vec4 filterColor(vec4 frag) {
    const vec3 lum = vec3(0.21, 0.71, 0.07);
    float grey = dot(frag.rgb, lum.rgb);
    return vec4(grey, grey, grey, frag.a);
}

vec3 getWorldPos(float depth) {
    vec3 cameraPos = CameraDir * depth;

    vec3 worldPos = (uInverseView * vec4(cameraPos, 1)).xyz;

    return worldPos;
}

float getDepth() {
    float depth = texture2D(uDepth, Vertex.zw).r;

    depth = linearizeDepth(depth, uNearPlane, uFarPlane);
    //depth = ( - uNearPlane) / (uFarPlane - uNearPlane);
    depth = depth / uFarPlane;

    return depth;
}

void main() {
    float depth = getDepth();
    vec3 worldPos = getWorldPos(depth);

    float distance = distance(uSphereCenter, worldPos);
    float strength = smoothstep(uSphereRadius - uSphereFeather, uSphereRadius + uSphereFeather, distance);

    vec4 fragColor = texture2D(uColor, Vertex.zw);

    gl_FragColor = mix(fragColor, filterColor(fragColor), strength);
    //gl_FragColor = vec4(worldPos, 1);
}
