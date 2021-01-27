package com.simibubi.create.foundation.render.gl.shader;

public enum Shader {
    ROTATING("shader/rotating.vert", "shader/instanced.frag"),
    BELT("shader/belt.vert", "shader/instanced.frag"),
    CONTRAPTION_STRUCTURE("shader/contraption.vert", "shader/contraption.frag"),
    CONTRAPTION_ROTATING("shader/contraption_rotating.vert", "shader/contraption.frag"),
    CONTRAPTION_BELT("shader/contraption_belt.vert", "shader/contraption.frag"),
    CONTRAPTION_ACTOR("shader/contraption_actor.vert", "shader/contraption.frag"),
    ;

    public final String vert;
    public final String frag;

    Shader(String vert, String frag) {
        this.vert = vert;
        this.frag = frag;
    }
}
