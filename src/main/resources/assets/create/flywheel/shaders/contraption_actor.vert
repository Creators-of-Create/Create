#version 110
#define PI 3.1415926538

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
varying vec3 BoxCoord;
varying vec2 Light;

uniform vec3 uLightBoxSize;
uniform vec3 uLightBoxMin;
uniform mat4 uModel;

uniform float uTime;
uniform mat4 uViewProjection;
uniform int uDebug;

uniform vec3 uCameraPos;

#if defined(USE_FOG)
varying float FragDistance;
#endif

void main() {
    float degrees = aOffset + uTime * aSpeed / 20.;
    //float angle = fract(degrees / 360.) * PI * 2.;

    vec4 kineticRot = quat(aAxis, degrees);
    vec3 rotated = rotateVertexByQuat(aPos - aRotationCenter, kineticRot) + aRotationCenter;
    vec3 localPos = rotateVertexByQuat(rotated - .5, aInstanceRot) + aInstancePos + .5;

    vec4 worldPos = uModel * vec4(localPos, 1.);

    vec3 norm = rotateVertexByQuat(rotateVertexByQuat(aNormal, kineticRot), aInstanceRot);
    norm = modelToNormal(uModel) * norm;

    BoxCoord = (worldPos.xyz - uLightBoxMin) / uLightBoxSize;
    Diffuse = diffuse(norm);
    TexCoords = aTexCoords;
    Light = aModelLight;
    gl_Position = uViewProjection * worldPos;

    #if defined(USE_FOG)
    FragDistance = length(worldPos.xyz);
    #endif

    if (uDebug == 2) {
        Color = vec4(norm, 1.);
    } else {
        Color = vec4(1.);
    }
}
