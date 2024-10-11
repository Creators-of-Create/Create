void flw_transformBoundingSphere(in FlwInstance instance, inout vec3 center, inout float radius) {
    // The instance will spin about the rotation center, so we need to expand the radius to account for that
    float extraForKinetic = length(center - instance.rotationCenter);
    float extraForModel = length(center - 0.5);

    radius += extraForKinetic + extraForModel;
    center += instance.pos;
}
