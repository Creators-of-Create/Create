#version 110
#define PI 3.1415926538

#flwinclude <"create:core/quaternion.glsl">
#flwinclude <"create:core/matutils.glsl">
#flwinclude <"create:core/diffuse.glsl">

attribute vec3 aPos;
attribute vec3 aNormal;
attribute vec2 aTexCoords;

attribute vec3 aInstancePos;
attribute vec2 aLight;
attribute vec3 aNetworkTint;
attribute float aSpeed;
attribute float aOffset;
attribute vec3 aAxis;

varying vec2 TexCoords;
varying vec4 Color;
varying float Diffuse;
varying vec2 Light;

#if defined(CONTRAPTION)
varying vec3 BoxCoord;

uniform vec3 uLightBoxSize;
uniform vec3 uLightBoxMin;
uniform mat4 uModel;
#endif

uniform float uTime;
uniform mat4 uViewProjection;
uniform int uDebug;

uniform vec3 uCameraPos;

#if defined(USE_FOG)
varying float FragDistance;
#endif

void main() {
    float degrees = aOffset + uTime * aSpeed * 3./10.;
    vec4 kineticRot = quat(aAxis, degrees);

    vec4 worldPos = vec4(rotateVertexByQuat(aPos - .5, kineticRot) + aInstancePos + .5, 1.);

    vec3 norm = rotateVertexByQuat(aNormal, kineticRot);

    #ifdef CONTRAPTION
    worldPos = uModel * worldPos;
    norm = modelToNormal(uModel) * norm;

    BoxCoord = (worldPos.xyz - uLightBoxMin) / uLightBoxSize;
    #if defined(USE_FOG)
    FragDistance = length(worldPos.xyz);
    #endif
    #elif defined(USE_FOG)
    FragDistance = length(worldPos.xyz - uCameraPos);
    #endif

    Diffuse = diffuse(norm);
    TexCoords = aTexCoords;
    Light = aLight;
    gl_Position = uViewProjection * worldPos;

    #ifdef CONTRAPTION
    if (uDebug == 2) {
        Color = vec4(norm, 1.);
    } else {
        Color = vec4(1.);
    }
    #else
    if (uDebug == 1) {
        Color = vec4(aNetworkTint, 1.);
    } else if (uDebug == 2) {
        Color = vec4(norm, 1.);
    } else {
        Color = vec4(1.);
    }
    #endif
}