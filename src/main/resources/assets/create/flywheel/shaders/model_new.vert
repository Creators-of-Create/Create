#flwbuiltins
#flwinclude <"create:core/diffuse.glsl">

#[VertexData]
struct Vertex {
#[Layout(float)]
    vec3 pos;
    vec3 normal;
    vec2 texCoords;
};

#[InstanceData]
struct Instance {
#[Normalized(ushort)]
    vec2 light;
    #[Normalized(ubyte)]
    vec4 color;
    mat4 transform;
    mat3 normalMat;
};

#[Fragment]
struct Raster {
    vec2 texCoords;
    vec4 color;
    float diffuse;
    vec2 light;
};

Raster FLWMain(Vertex v, Instance i) {
    vec4 worldPos = i.transform * vec4(v.pos, 1.);

    vec3 norm = i.normalMat * v.normal;
    norm = normalize(norm);

    FLWFinalizeWorldPos(worldPos);
    FLWFinalizeNormal(norm);

    Raster r;
    r.diffuse = diffuse(norm);
    r.texCoords = v.texCoords;
    r.light = i.light;
    r.color = i.color;

    return r;
}
