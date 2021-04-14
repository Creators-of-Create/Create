#version 140

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
    WorldDir = (uInverseView * vec4(cameraDir, 1.)).xyz;
    //worldDirection = (uInverseProjection * vec4(aVertex.xy, 0, 1.)).xyz;
}
