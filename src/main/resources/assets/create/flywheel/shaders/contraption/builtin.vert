#if defined(USE_FOG)
varying float FragDistance;
#endif

varying vec3 BoxCoord;

uniform vec3 uLightBoxSize;
uniform vec3 uLightBoxMin;
uniform mat4 uModel;

void FLWFinalizeWorldPos(inout vec4 worldPos, vec3 cameraPos) {
    worldPos = uModel * worldPos;

    BoxCoord = (worldPos.xyz - uLightBoxMin) / uLightBoxSize;

    #if defined(USE_FOG)
    FragDistance = length(worldPos.xyz);
    #endif
}

void FLWFinalizeNormal(inout vec3 normal) {
    mat3 m;
    m[0] = uModel[0].xyz;
    m[1] = uModel[1].xyz;
    m[2] = uModel[2].xyz;
    normal = m * normal;
}

