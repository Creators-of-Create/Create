uniform vec3 create_oneOverLightBoxSize;
uniform vec3 create_lightVolumeMin;
uniform mat4 create_model;
uniform mat3 create_normal;

out vec3 create_lightVolumeCoord;

void flw_beginVertex() {
}

void flw_endVertex() {
    create_lightVolumeCoord = (flw_vertexPos.xyz - create_lightVolumeMin) * create_oneOverLightBoxSize;

    flw_vertexPos = create_model * flw_vertexPos;
    flw_vertexNormal = create_normal * flw_vertexNormal;
}
