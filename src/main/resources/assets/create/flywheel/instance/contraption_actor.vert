#include "flywheel:util/matrix.glsl"
#include "flywheel:util/quaternion.glsl"

const float uTime = 0.;

void flw_instanceVertex(in FlwInstance instance) {
    float degrees = instance.offset + uTime * instance.speed / 20.;

    vec4 kineticRot = quat(instance.axis, degrees);
    vec3 rotated = rotateVertexByQuat(flw_vertexPos.xyz - instance.rotationCenter, kineticRot) + instance.rotationCenter;

    flw_vertexPos = vec4(rotateVertexByQuat(rotated - .5, instance.rotation) + instance.pos + .5, 1.);
    flw_vertexNormal = rotateVertexByQuat(rotateVertexByQuat(flw_vertexNormal, kineticRot), instance.rotation);
    flw_vertexLight = instance.light;
}
