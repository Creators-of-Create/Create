#version 110
#define PI 3.1415926538

#flwinclude <"create:core/matutils.glsl">
#flwinclude <"create:core/quaternion.glsl">
#flwinclude <"create:core/diffuse.glsl">

attribute vec3 aPos;
attribute vec3 aNormal;
attribute vec2 aTexCoords;

attribute vec3 aInstancePos;
attribute vec2 aLight;

attribute vec3 aSegmentOffset;
attribute vec3 aPivot;
attribute float aHorizontalAngle;
attribute float aIntensity;
attribute float aFlapScale;

attribute float aFlapness;

// outputs
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
#flwinclude <"create:contraption/builtin.vert">
#else
#flwinclude <"create:std/builtin.vert">
#endif

float toRad(float degrees) {
    return fract(degrees / 360.) * PI * 2.;
}

float getFlapAngle() {
    float absFlap = abs(aFlapness);

    float angle = sin((1. - absFlap) * PI * aIntensity) * 30. * aFlapness * aFlapScale;

    float halfAngle = angle * 0.5;

    float which = step(0., aFlapness);
    float degrees = which * halfAngle + (1. - which) * angle; // branchless conditional multiply

    return degrees;
}

void main() {
    float flapAngle = getFlapAngle();

    vec4 orientation = quat(vec3(0., 1., 0.), -aHorizontalAngle);
    vec4 flapRotation = quat(vec3(1., 0., 0.), flapAngle);

    vec3 rotated = rotateVertexByQuat(aPos - aPivot, flapRotation) + aPivot + aSegmentOffset;
    rotated = rotateVertexByQuat(rotated - .5, orientation) + aInstancePos + .5;

    vec4 worldPos = vec4(rotated, 1.);
    vec3 norm = rotateVertexByQuat(rotateVertexByQuat(aNormal, flapRotation), orientation);

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    Diffuse = diffuse(norm);
    TexCoords = aTexCoords;
    Light = aLight;
    gl_Position = uViewProjection * worldPos;

    Color = vec4(1.);
}
