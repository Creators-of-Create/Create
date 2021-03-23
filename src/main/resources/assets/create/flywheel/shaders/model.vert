#version 110

#flwinclude <"create:core/matutils.glsl">
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
    vec4 worldPos = aTransform * vec4(aPos, 1.);

    vec3 norm = aNormalMat * aNormal;

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

    norm = normalize(norm);

    Diffuse = diffuse(norm);
    TexCoords = aTexCoords;
    Light = aLight;
    gl_Position = uViewProjection * worldPos;

    Color = aColor;
}