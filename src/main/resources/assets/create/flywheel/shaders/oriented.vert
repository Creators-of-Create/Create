#version 110

#flwbuiltins
#flwinclude <"create:core/matutils.glsl">
#flwinclude <"create:core/quaternion.glsl">
#flwinclude <"create:core/diffuse.glsl">

attribute vec3 aPos;
attribute vec3 aNormal;
attribute vec2 aTexCoords;

attribute vec2 aLight;
attribute vec4 aColor;
attribute vec3 aInstancePos;
attribute vec3 aPivot;
attribute vec4 aRotation;

varying vec2 TexCoords;
varying vec4 Color;
varying float Diffuse;
varying vec2 Light;

void main() {
    vec4 worldPos = vec4(rotateVertexByQuat(aPos - aPivot, aRotation) + aPivot + aInstancePos, 1.);

    vec3 norm = rotateVertexByQuat(aNormal, aRotation);

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    Diffuse = diffuse(norm);
    TexCoords = aTexCoords;
    Light = aLight;

    Color = aColor;
}
