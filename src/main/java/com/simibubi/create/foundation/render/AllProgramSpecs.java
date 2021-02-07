package com.simibubi.create.foundation.render;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.render.contraption.ContraptionProgram;
import com.simibubi.create.foundation.render.gl.BasicProgram;
import com.simibubi.create.foundation.render.gl.backend.Backend;
import com.simibubi.create.foundation.render.gl.shader.GlProgram;
import com.simibubi.create.foundation.render.gl.shader.ProgramSpec;
import com.simibubi.create.foundation.render.gl.shader.ShaderConstants;
import net.minecraft.util.ResourceLocation;

public class AllProgramSpecs {
    public static final ProgramSpec<BasicProgram> ROTATING = register(new ProgramSpec<>("rotating", Locations.ROTATING, Locations.INSTANCED, BasicProgram::new));
    public static final ProgramSpec<BasicProgram> BELT = register(new ProgramSpec<>("belt", Locations.BELT, Locations.INSTANCED, BasicProgram::new));
    public static final ProgramSpec<ContraptionProgram> CONTRAPTION_STRUCTURE = register(new ProgramSpec<>("contraption_structure", Locations.CONTRAPTION_STRUCTURE, Locations.CONTRAPTION, ContraptionProgram::new));
    public static final ProgramSpec<ContraptionProgram> CONTRAPTION_ROTATING = register(new ProgramSpec<>("contraption_rotating", Locations.ROTATING, Locations.CONTRAPTION, ContraptionProgram::new, ShaderConstants.define("CONTRAPTION")));
    public static final ProgramSpec<ContraptionProgram> CONTRAPTION_BELT = register(new ProgramSpec<>("contraption_belt", Locations.BELT, Locations.CONTRAPTION, ContraptionProgram::new, ShaderConstants.define("CONTRAPTION")));
    public static final ProgramSpec<ContraptionProgram> CONTRAPTION_ACTOR = register(new ProgramSpec<>("contraption_actor", Locations.CONTRAPTION_ACTOR, Locations.CONTRAPTION, ContraptionProgram::new));

    private static <P extends GlProgram, S extends ProgramSpec<P>> S register(S spec) {
        return Backend.register(spec);
    }

    public static class Locations {
        public static final ResourceLocation INSTANCED = loc("instanced.frag");
        public static final ResourceLocation CONTRAPTION = loc("contraption.frag");

        public static final ResourceLocation ROTATING = loc("rotating.vert");
        public static final ResourceLocation BELT = loc("belt.vert");
        public static final ResourceLocation CONTRAPTION_STRUCTURE = loc("contraption_structure.vert");
        public static final ResourceLocation CONTRAPTION_ACTOR = loc("contraption_actor.vert");


        private static ResourceLocation loc(String name) {
            return new ResourceLocation(Create.ID, "shader/" + name);
        }
    }
}
