package com.simibubi.create.foundation.render.gl.shader;

import com.simibubi.create.Create;
import net.minecraft.util.ResourceLocation;

public enum AllShaderPrograms {
    ROTATING("shader/rotating.vert", "shader/instanced.frag"),
    BELT("shader/belt.vert", "shader/instanced.frag"),
    CONTRAPTION_STRUCTURE("shader/contraption_structure.vert", "shader/contraption_structure.frag"),
    CONTRAPTION_ROTATING("shader/contraption_rotating.vert", "shader/contraption.frag"),
    CONTRAPTION_BELT("shader/contraption_belt.vert", "shader/contraption.frag"),
    CONTRAPTION_ACTOR("shader/contraption_actor.vert", "shader/contraption.frag"),
    ;

    public final String vert;
    public final String frag;

    AllShaderPrograms(String vert, String frag) {
        this.vert = vert;
        this.frag = frag;
    }
}

//public class AllShaderPrograms {
//    public static final ProgramBuilder ROTATING = new ProgramBuilder(name("rotating"))
//            .vert(vert("rotating"))
//            .frag(frag("instanced"));
//    public static final ProgramBuilder BELT = new ProgramBuilder(name("belt"))
//            .vert(vert("belt"))
//            .frag(frag("instanced"));
//    public static final ProgramBuilder CONTRAPTION_STRUCTURE = new ProgramBuilder(name("contraption_structure"))
//            .vert(vert("contraption_structure"))
//            .frag(frag("contraption_structure"));
//    public static final ProgramBuilder CONTRAPTION_ROTATING = new ProgramBuilder(name("contraption_rotating"))
//            .vert(vert("contraption_rotating"))
//            .frag(frag("contraption"));
//    public static final ProgramBuilder CONTRAPTION_BELT = new ProgramBuilder(name("contraption_belt"))
//            .vert(vert("contraption_belt"))
//            .frag(frag("contraption"));
//    public static final ProgramBuilder CONTRAPTION_ACTOR = new ProgramBuilder(name("contraption_actor"))
//            .vert(vert("contraption_actor"))
//            .frag(frag("contraption"));
//
//    private static ResourceLocation vert(String file) {
//        return new ResourceLocation(Create.ID, "shader/" + file + ".vert");
//    }
//
//    private static ResourceLocation frag(String file) {
//        return new ResourceLocation(Create.ID, "shader/" + file + ".vert");
//    }
//
//    private static ResourceLocation name(String name) {
//        return new ResourceLocation(Create.ID, name);
//    }
//}
