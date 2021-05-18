uniform float uTime;
uniform mat4 uViewProjection;
uniform vec3 uCameraPos;

#if defined(USE_FOG)
varying float FragDistance;
#endif

void FLWFinalizeWorldPos(inout vec4 worldPos) {
    #if defined(USE_FOG)
    FragDistance = length(worldPos.xyz - uCameraPos);
    #endif

    gl_Position = uViewProjection * worldPos;
}

void FLWFinalizeNormal(inout vec3 normal) {
    // noop
}
