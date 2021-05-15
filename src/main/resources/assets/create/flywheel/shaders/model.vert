#version 110

#flwbuiltins
#flwinclude <"create:core/diffuse.glsl">

attribute vec3 aPos;
attribute vec3 aNormal;
attribute vec2 aTexCoords;

attribute vec2 aLight;
attribute vec4 aColor;
attribute mat4 aTransform;
attribute mat3 aNormalMat;

varying vec2 TexCoords;
varying vec4 Color;
varying float Diffuse;
varying vec2 Light;

void main() {
    vec4 worldPos = aTransform * vec4(aPos, 1.);

    vec3 norm = aNormalMat * aNormal;

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    norm = normalize(norm);

    Diffuse = diffuse(norm);
    TexCoords = aTexCoords;
    Light = aLight;

    Color = aColor;
}
