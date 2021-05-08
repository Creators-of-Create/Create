#version 110
#define PI 3.1415926538

#flwbuiltins
#flwinclude <"create:core/matutils.glsl">
#flwinclude <"create:core/quaternion.glsl">
#flwinclude <"create:core/diffuse.glsl">

// model data
attribute vec3 aPos;
attribute vec3 aNormal;
attribute vec2 aTexCoords;

// instance data
attribute vec3 aInstancePos;
attribute vec2 aModelLight;
attribute float aOffset;
attribute vec3 aAxis;
attribute vec4 aInstanceRot;
attribute vec3 aRotationCenter;
attribute float aSpeed;

varying float Diffuse;
varying vec2 TexCoords;
varying vec4 Color;
varying vec2 Light;

void main() {
    float degrees = aOffset + uTime * aSpeed / 20.;
    //float angle = fract(degrees / 360.) * PI * 2.;

    vec4 kineticRot = quat(aAxis, degrees);
    vec3 rotated = rotateVertexByQuat(aPos - aRotationCenter, kineticRot) + aRotationCenter;

    vec4 worldPos = vec4(rotateVertexByQuat(rotated - .5, aInstanceRot) + aInstancePos + .5, 1.);
    vec3 norm = rotateVertexByQuat(rotateVertexByQuat(aNormal, kineticRot), aInstanceRot);

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    Diffuse = diffuse(norm);
    TexCoords = aTexCoords;
    Light = aModelLight;

    if (uDebug == 2) {
        Color = vec4(norm, 1.);
    } else {
        Color = vec4(1.);
    }
}
