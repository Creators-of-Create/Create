void flw_transformBoundingSphere(in FlwInstance instance, inout vec3 center, inout float radius) {
    radius += length(center - 0.5);
    center += instance.pos;
}
