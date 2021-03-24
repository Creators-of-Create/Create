#version 110
#define PI 3.1415926538

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
attribute vec4 aInstanceRot;
attribute vec2 aSourceTexture;
attribute vec4 aScrollTexture;
attribute float aScrollMult;

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
    vec3 rotated = rotateVertexByQuat(aPos - .5, aInstanceRot) + aInstancePos + .5;

    vec4 worldPos = vec4(rotated, 1.);

    vec3 norm = rotateVertexByQuat(aNormal, aInstanceRot);

#ifdef CONTRAPTION
    worldPos = uModel * worldPos;
    norm = normalize(modelToNormal(uModel) * norm);

    BoxCoord = (worldPos.xyz - uLightBoxMin) / uLightBoxSize;
    #if defined(USE_FOG)
    FragDistance = length(worldPos.xyz);
    #endif
#elif defined(USE_FOG)
    FragDistance = length(worldPos.xyz - uCameraPos);
#endif

    float scrollSize = aScrollTexture.w - aScrollTexture.y;
    float scroll = fract(aSpeed * uTime / (31.5 * 16.) + aOffset) * scrollSize * aScrollMult;

    Diffuse = diffuse(norm);
    TexCoords = aTexCoords - aSourceTexture + aScrollTexture.xy + vec2(0, scroll);
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
        Color = aColor;
    } else if (uDebug == 2) {
        Color = vec4(norm, 1.);
    } else {
        Color = vec4(1.);
    }
    #endif
}
