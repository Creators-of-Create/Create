
#if defined(USE_FOG)
varying float FragDistance;
#endif

void FLWFinalizeWorldPos(inout vec4 worldPos, vec3 cameraPos) {
    #if defined(USE_FOG)
    FragDistance = length(worldPos.xyz - cameraPos);
    #endif
}

void FLWFinalizeNormal(inout vec3 normal) {
    // noop
}
