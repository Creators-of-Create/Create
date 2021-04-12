#version 120

attribute vec4 aVertex;// <vec2 position, vec2 texCoords>

varying vec4 Vertex;
varying vec3 CameraDir;

uniform mat4 uInverseProjection;

void main() {
    gl_Position = vec4(aVertex.xy, 0.0f, 1.0f);
    Vertex = aVertex;

    vec4 clip = vec4(aVertex.xy, 0, 1);

    clip *= uInverseProjection;

    CameraDir = clip.xyz / clip.w;
    //worldDirection = (uInverseProjection * vec4(aVertex.xy, 0, 1.)).xyz;
}
