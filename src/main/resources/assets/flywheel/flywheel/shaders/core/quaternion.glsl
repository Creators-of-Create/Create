
#define PIOVER2 1.5707963268

vec4 quat(vec3 axis, float angle) {
    float halfAngle = angle * PIOVER2 / 180.0;
    vec2 cs = sin(vec2(PIOVER2 - halfAngle, halfAngle)); // compute sin and cos in one instruction
    return vec4(axis.xyz * cs.y,  cs.x);
}

vec4 quatMult(vec4 q1, vec4 q2) {
    // disgustingly vectorized quaternion multiplication
    vec4 a = q1.w * q2.xyzw;
    vec4 b = q1.x * q2.wzxy * vec4(1., -1., 1., -1.);
    vec4 c = q1.y * q2.zwxy * vec4(1., 1., -1., -1.);
    vec4 d = q1.z * q2.yxwz * vec4(-1., 1., 1., -1.);

    return a + b + c + d;
}

vec3 rotateVertexByQuat(vec3 v, vec4 q) {
    vec3 i = q.xyz;
    return v + 2.0 * cross(i, cross(i, v) + q.w * v);
}

vec3 rotateAbout(vec3 v, vec3 axis, float angle) {
    return rotateVertexByQuat(v, quat(axis, angle));
}
