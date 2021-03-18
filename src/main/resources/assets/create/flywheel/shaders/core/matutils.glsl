
//mat4 rotate(vec3 axis, float angle) {
//    float s = sin(angle);
//    float c = cos(angle);
//    float oc = 1. - c;
//
//    return mat4(
//    oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.,
//    oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.,
//    oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.,
//    0.,                                 0.,                                 0.,                                 1.
//    );
//}

mat4 rotate(vec3 axis, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1. - c;

    vec3 sa = axis * s;

    mat4 mr = mat4(1.);
    mr[0].xyz = oc * axis.xxz * axis.xyx + vec3(c, sa.z, -sa.y);
    mr[1].xyz = oc * axis.xyy * axis.yyz + vec3(-sa.z, c, sa.x);
    mr[2].xyz = oc * axis.zyz * axis.xzz + vec3(sa.y, -sa.x, c);

    return mr;
}

mat4 rotation(vec3 rot) {
    return rotate(vec3(0., 1., 0.), rot.y) * rotate(vec3(0., 0., 1.), rot.z) * rotate(vec3(1., 0., 0.), rot.x);
}

mat3 modelToNormal(mat4 mat) {
    // Discard the edges. This won't be accurate for scaled or skewed matrices,
    // but we don't have to work with those often.
    mat3 m;
    m[0] = mat[0].xyz;
    m[1] = mat[1].xyz;
    m[2] = mat[2].xyz;
    return m;
}