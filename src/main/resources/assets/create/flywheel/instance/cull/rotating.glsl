void flw_transformBoundingSphere(in FlwInstance instance, inout vec3 center, inout float radius) {
    // The instance will spin about (0.5, 0.5, 0.5), so we need to expand the radius to account for that
    radius += length(center - 0.5);
    center += instance.pos;
}
