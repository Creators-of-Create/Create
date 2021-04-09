#version 120

layout (std140) struct Sphere {
    vec4 positionRadius;
    vec4 color;
} uSpheres;

uniform sampler2D uDepth;
uniform sampler2D uColor;

void main() {
    gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
}
