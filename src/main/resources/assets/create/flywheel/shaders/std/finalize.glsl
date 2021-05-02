void FLWFinalizeWorldPos(inout vec4 worldPos) {
    #if defined(USE_FOG)

    FragDistance = length(worldPos.xyz - uCameraPos);
    #endif
}

void FLWFinalizeNormal(inout vec3 normal) {
    // noop
}
