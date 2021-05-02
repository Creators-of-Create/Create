varying vec3 BoxCoord;

uniform vec3 uLightBoxSize;
uniform vec3 uLightBoxMin;
uniform mat4 uModel;

void FLWFinalizeWorldPos(inout vec4 worldPos) {
    worldPos = uModel * worldPos;

    BoxCoord = (worldPos.xyz - uLightBoxMin) / uLightBoxSize;
    #if defined(USE_FOG)

    FragDistance = length(worldPos.xyz);
    #endif
}

void FLWFinalizeNormal(inout vec3 normal) {
    normal = modelToNormal(uModel) * normal;
}

