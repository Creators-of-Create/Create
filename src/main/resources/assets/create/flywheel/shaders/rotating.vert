#version 110
#define PI 3.1415926538

#flwbuiltins
#flwinclude <"create:core/quaternion.glsl">
#flwinclude <"create:core/matutils.glsl">
#flwinclude <"create:core/diffuse.glsl">

attribute vec3 aPos;
attribute vec3 aNormal;
attribute vec2 aTexCoords;

attribute vec2 aLight;
attribute vec4 aColor;
attribute vec3 aInstancePos;
attribute float aSpeed;
attribute float aOffset;
attribute vec3 aAxis;

varying vec2 TexCoords;
varying vec4 Color;
varying float Diffuse;
varying vec2 Light;

mat4 kineticRotation() {
    float degrees = aOffset + uTime * aSpeed * 3./10.;
    float angle = fract(degrees / 360.) * PI * 2.;

    return rotate(aAxis, angle);
}

void main() {
    mat4 kineticRotation = kineticRotation();
    vec4 worldPos = kineticRotation * vec4(aPos - .5, 1.) + vec4(aInstancePos + .5, 0.);

    vec3 norm = modelToNormal(kineticRotation) * aNormal;

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    Diffuse = diffuse(norm);
    TexCoords = aTexCoords;
    Light = aLight;

    #ifdef CONTRAPTION
    if (uDebug == 2) {
        Color = vec4(norm, 1.);
    } else {
        Color = vec4(1.);
    }
    #else
    if (uDebug == 1) {
        Color = aColor;
    } else if (uDebug == 2) {
        Color = vec4(norm, 1.);
    } else {
        Color = vec4(1.);
    }
    #endif
}
