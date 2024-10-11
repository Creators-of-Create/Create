#include "flywheel:util/matrix.glsl"
#include "flywheel:util/quaternion.glsl"

void flw_instanceVertex(in FlwInstance instance) {
    float degrees = instance.offset + flw_renderSeconds * instance.speed;

    vec4 kineticRot = quaternionDegrees(instance.axis, degrees);
    vec3 rotated = rotateByQuaternion(flw_vertexPos.xyz - instance.rotationCenter, kineticRot) + instance.rotationCenter;

    flw_vertexPos.xyz = rotateByQuaternion(rotated - .5, instance.rotation) + instance.pos + .5;
    flw_vertexNormal = rotateByQuaternion(rotateByQuaternion(flw_vertexNormal, kineticRot), instance.rotation);
    flw_vertexLight = vec2(instance.light) / 256.;
}
