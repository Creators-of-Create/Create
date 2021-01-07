#version 330 core
#define PI 3.1415926538
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

layout (location = 3) in vec3 instancePos;
layout (location = 4) in vec2 light;
layout (location = 5) in float speed;
layout (location = 6) in float rotationOffset;
layout (location = 7) in vec3 rotationAxis;

out vec3 Normal;
out vec2 TexCoords;
out vec2 Light;

uniform float time;
uniform int ticks;
uniform mat4 projection;
uniform mat4 view;

mat4 kineticRotation() {
    float degrees = rotationOffset + time * speed * -3./10.;
    float angle = fract(degrees / 360.) * PI * 2.;

    vec3 axis = normalize(rotationAxis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.,
                0.,                                 0.,                                 0.,                                 1.);
}

void main() {
    mat4 rotation = kineticRotation();
    vec4 renderPos = rotation * vec4(aPos - vec3(0.5), 1);

    renderPos += vec4(instancePos + vec3(0.5), 0);

    TexCoords = aTexCoords;
    Normal = normalize((rotation * vec4(aNormal, 0.)).xyz);
    gl_Position = projection * view * renderPos;
    Light = light;
}