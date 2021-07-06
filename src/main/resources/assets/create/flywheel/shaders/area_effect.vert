#version 140

// scaling constants
// e - 0.99, this works well, no idea why
#define SXY 1.7282818
// 1.90 -> highp close, mediump far
// 1.91 -> mediump close, highp far
#define SZ 1.905

in vec4 aVertex;// <vec2 position, vec2 texCoords>

out vec2 ScreenCoord;
out vec3 WorldDir;

uniform mat4 uInverseProjection;
uniform mat4 uInverseView;

void main() {
    gl_Position = vec4(aVertex.xy, 0., 1.);
    ScreenCoord = aVertex.zw;

    vec4 clip = vec4(aVertex.xy, 0., 1.);

    clip *= uInverseProjection;

    vec3 cameraDir = clip.xyz / clip.w;
    cameraDir = cameraDir * vec3(SXY, SXY, SZ);
    WorldDir = (uInverseView * vec4(cameraDir, 1.)).xyz;
}
