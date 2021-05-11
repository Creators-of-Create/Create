#version 450
#extension GL_NV_mesh_shader : require

layout(local_size_x=32) in;

layout(max_vertices=64, max_primitives=32) out;

layout (std430, binding = 1) buffer _vertices {
    FLWVertexData vertices[];
} vb;

struct s_meshlet {
    uint vertices[64];
    uint indices[96];
    uint vertex_count;
    uint index_count;
};

layout (std430, binding = 2) buffer _meshlets {
    s_meshlet meshlets[];
} mbuf;

layout (location = 0) out PerVertexData {
    vec4 color;
} v_out[];// [max_vertices]

void main() {
    uint mi = gl_WorkGroupID.x;
    uint thread_id = gl_LocalInvocationID.x;

    uint primIdx = thread_id * 3;
    uint vertStartIdx = thread_id * 2;

    gl_MeshVerticesNV[vertStartIdx + 0].gl_Position;
    gl_MeshVerticesNV[vertStartIdx + 1].gl_Position;

    gl_PrimitiveIndicesNV[primIdx + 0] = mbuf.meshlets[mi].indices[primIdx + 0];
    gl_PrimitiveIndicesNV[primIdx + 1] = mbuf.meshlets[mi].indices[primIdx + 1];
    gl_PrimitiveIndicesNV[primIdx + 2] = mbuf.meshlets[mi].indices[primIdx + 2];

    gl_PrimitiveCountNV = mbuf.meshlets[mi].vertex_count / 2;

}
