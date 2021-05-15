#version 110
#define PI 3.1415926538

#flwbuiltins
#flwinclude <"create:core/matutils.glsl">
#flwinclude <"create:core/diffuse.glsl">

attribute vec3 aPos;
attribute vec3 aNormal;
attribute vec2 aTexCoords;
attribute vec4 aColor;
attribute vec2 aModelLight;

varying float Diffuse;
varying vec2 TexCoords;
varying vec4 Color;
varying vec2 Light;

void main() {
    vec4 worldPos = vec4(aPos, 1.);
    vec3 norm = aNormal;

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    Diffuse = diffuse(norm);
    if (uDebug == 2) {
        Color = vec4(norm, 1.);
    } else {
        Color = aColor / diffuse(aNormal);
    }
    TexCoords = aTexCoords;
    Light = aModelLight;
}
