package com.simibubi.create.foundation.utility.render.shader;

public enum Shader {
    ROTATING_INSTANCED("shader/rotating.vert", "shader/instanced.frag"),
    BELT_INSTANCED("shader/belt.vert", "shader/instanced.frag"),;

    public final String vert;
    public final String frag;

    Shader(String vert, String frag) {
        this.vert = vert;
        this.frag = frag;
    }
}
