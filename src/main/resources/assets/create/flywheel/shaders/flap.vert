#define PI 3.1415926538

#flwbuiltins
#flwinclude <"flywheel:core/matutils.glsl">
#flwinclude <"flywheel:core/quaternion.glsl">
#flwinclude <"flywheel:core/diffuse.glsl">

#[InstanceData]
struct Flap {
    vec3 instancePos;
    vec2 light;
    vec3 segmentOffset;
    vec3 pivot;
    float horizontalAngle;
    float intensity;
    float flapScale;
    float flapness;
};

#flwinclude <"flywheel:data/modelvertex.glsl">
#flwinclude <"flywheel:data/blockfragment.glsl">


float toRad(float degrees) {
    return fract(degrees / 360.) * PI * 2.;
}

float getFlapAngle(float flapness, float intensity, float scale) {
    float absFlap = abs(flapness);

    float angle = sin((1. - absFlap) * PI * intensity) * 30. * flapness * scale;

    float halfAngle = angle * 0.5;

    float which = step(0., flapness);// 0 if negative, 1 if positive
    float degrees = which * halfAngle + (1. - which) * angle;// branchless conditional multiply

    return degrees;
}

BlockFrag FLWMain(Vertex v, Flap flap) {
    float flapAngle = getFlapAngle(flap.flapness, flap.intensity, flap.flapScale);

    vec4 orientation = quat(vec3(0., 1., 0.), -flap.horizontalAngle);
    vec4 flapRotation = quat(vec3(1., 0., 0.), flapAngle);

    vec3 rotated = rotateVertexByQuat(v.pos - flap.pivot, flapRotation) + flap.pivot + flap.segmentOffset;
    rotated = rotateVertexByQuat(rotated - .5, orientation) + flap.instancePos + .5;

    vec4 worldPos = vec4(rotated, 1.);
    vec3 norm = rotateVertexByQuat(rotateVertexByQuat(v.normal, flapRotation), orientation);

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    BlockFrag b;
    b.diffuse = diffuse(norm);
    b.texCoords = v.texCoords;
    b.light = flap.light;
    #if defined(DEBUG_NORMAL)
    b.color = vec4(norm, 1.);
    #else
    b.color = vec4(1.);
    #endif
    return b;
}
