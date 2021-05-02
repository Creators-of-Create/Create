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

uniform float uTime;
uniform mat4 uViewProjection;
uniform int uDebug;

uniform vec3 uCameraPos;

#if defined(USE_FOG)
varying float FragDistance;
#endif

#ifdef CONTRAPTION
#flwinclude <"create:contraption/finalize.glsl">
#else
#flwinclude <"create:std/finalize.glsl">
#endif


void main() {
    vec4 worldPos = aTransform * vec4(aPos, 1.);

    vec3 norm = aNormalMat * aNormal;

    FLWFinalizeNormal(norm);
    FLWFinalizeWorldPos(worldPos);

    norm = normalize(norm);

    Diffuse = diffuse(norm);
    TexCoords = aTexCoords;
    Light = aLight;
    gl_Position = uViewProjection * worldPos;

    Color = aColor;
}
