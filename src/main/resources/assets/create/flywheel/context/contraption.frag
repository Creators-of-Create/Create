uniform sampler3D create_lightVolume;

in vec3 create_lightVolumeCoord;

void flw_beginFragment() {
    flw_fragLight = max(flw_fragLight, texture(create_lightVolume, create_lightVolumeCoord).rg);
}

void flw_endFragment() {
}
