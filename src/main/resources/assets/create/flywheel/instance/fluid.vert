#include "flywheel:util/quaternion.glsl"
#include "flywheel:util/matrix.glsl"

void flw_instanceVertex(in FlwInstance instance) {
    flw_vertexPos.y *= instance.progress;
    
    flw_vertexTexCoord.t = instance.v0 + flw_vertexPos.y * instance.vScale;

    flw_vertexColor = instance.color;
    flw_vertexPos = instance.pose * flw_vertexPos;
    flw_vertexNormal = mat3(transpose(inverse(instance.pose))) * flw_vertexNormal;
    flw_vertexOverlay = instance.overlay;
    flw_vertexLight = max(vec2(instance.light) / 256., flw_vertexLight);
}
