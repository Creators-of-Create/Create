#version 110

attribute vec3 aPos;
attribute vec3 aNormal;
attribute vec2 aTexCoords;

attribute mat4 aTransform;
attribute mat3 aNormalMat;
attribute vec2 aLight;
attribute vec4 aColor;

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

mat3 modelToNormal(mat4 mat) {
    // Discard the edges. This won't be accurate for scaled or skewed matrices,
    // but we don't have to work with those often.
    mat3 m;
    m[0] = mat[0].xyz;
    m[1] = mat[1].xyz;
    m[2] = mat[2].xyz;
    return m;
}

float diffuse(vec3 normal) {
    float x = normal.x;
    float y = normal.y;
    float z = normal.z;
    return min(x * x * .6 + y * y * ((3. + y) / 4.) + z * z * .8, 1.);
}

void main() {
    vec4 worldPos = aTransform * vec4(aPos, 1.);

    mat3 normalMat = aNormalMat;

#ifdef CONTRAPTION
    worldPos = uModel * worldPos;
    normalMat *= modelToNormal(uModel);

    BoxCoord = (worldPos.xyz - uLightBoxMin) / uLightBoxSize;
    #if defined(USE_FOG)
    FragDistance = length(worldPos.xyz);
    #endif
#elif defined(USE_FOG)
    FragDistance = length(worldPos.xyz - uCameraPos);
#endif

    vec3 norm = normalize(normalMat * aNormal);

    Diffuse = diffuse(norm);
    TexCoords = aTexCoords;
    Light = aLight;
    gl_Position = uViewProjection * worldPos;

    Color = aColor;
}