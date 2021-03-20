package com.simibubi.create.foundation.render;

import static com.simibubi.create.foundation.render.backend.Backend.register;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticVertexAttributes;
import com.simibubi.create.content.contraptions.base.RotatingVertexAttributes;
import com.simibubi.create.content.contraptions.components.actors.ActorVertexAttributes;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionProgram;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionVertexAttributes;
import com.simibubi.create.content.contraptions.relays.belt.BeltVertexAttributes;
import com.simibubi.create.content.logistics.block.FlapVertexAttributes;
import com.simibubi.create.foundation.render.backend.gl.BasicProgram;
import com.simibubi.create.foundation.render.backend.gl.attrib.InstanceVertexAttributes;
import com.simibubi.create.foundation.render.backend.gl.attrib.ModelVertexAttributes;
import com.simibubi.create.foundation.render.backend.gl.shader.ProgramSpec;
import com.simibubi.create.foundation.render.backend.gl.shader.ShaderConstants;

import net.minecraft.util.ResourceLocation;

public class AllProgramSpecs {
    public static void init() {
        // noop, make sure the static field are loaded.
    }

    public static final ProgramSpec<BasicProgram> MODEL = register(ProgramSpec.builder("model", BasicProgram::new)
                                                                                 .addAttributes(ModelVertexAttributes.class)
                                                                                 .addAttributes(InstanceVertexAttributes.class)
                                                                                 .setVert(Locations.MODEL_VERT)
                                                                                 .setFrag(Locations.MODEL_FRAG)
                                                                                 .createProgramSpec());

    public static final ProgramSpec<BasicProgram> ROTATING = register(ProgramSpec.builder("rotating", BasicProgram::new)
                                                                                 .addAttributes(ModelVertexAttributes.class)
                                                                                 .addAttributes(KineticVertexAttributes.class)
                                                                                 .addAttributes(RotatingVertexAttributes.class)
                                                                                 .setVert(Locations.ROTATING)
                                                                                 .setFrag(Locations.MODEL_FRAG)
                                                                                 .createProgramSpec());

    public static final ProgramSpec<BasicProgram> BELT = register(ProgramSpec.builder("belt", BasicProgram::new)
                                                                             .addAttributes(ModelVertexAttributes.class)
                                                                             .addAttributes(KineticVertexAttributes.class)
                                                                             .addAttributes(BeltVertexAttributes.class)
                                                                             .setVert(Locations.BELT)
                                                                             .setFrag(Locations.MODEL_FRAG)
                                                                             .createProgramSpec());

    public static final ProgramSpec<BasicProgram> FLAPS = register(ProgramSpec.builder("flap", BasicProgram::new)
                                                                              .addAttributes(ModelVertexAttributes.class)
                                                                              .addAttributes(FlapVertexAttributes.class)
                                                                              .setVert(Locations.FLAP)
                                                                              .setFrag(Locations.MODEL_FRAG)
                                                                              .createProgramSpec());
    public static final ProgramSpec<ContraptionProgram> C_STRUCTURE = register(ProgramSpec.builder("contraption_structure", ContraptionProgram::new)
                                                                                      .addAttributes(ContraptionVertexAttributes.class)
                                                                                      .setVert(Locations.CONTRAPTION_STRUCTURE)
                                                                                      .setFrag(Locations.CONTRAPTION)
                                                                                      .createProgramSpec());
    public static final ProgramSpec<ContraptionProgram> C_MODEL = register(ProgramSpec.builder("contraption_model", ContraptionProgram::new)
                                                                                      .addAttributes(ModelVertexAttributes.class)
                                                                                      .addAttributes(InstanceVertexAttributes.class)
                                                                                      .setVert(Locations.MODEL_VERT)
                                                                                      .setFrag(Locations.CONTRAPTION)
                                                                                      .setDefines(ShaderConstants.define("CONTRAPTION"))
                                                                                      .createProgramSpec());
    public static final ProgramSpec<ContraptionProgram> C_ROTATING = register(ProgramSpec.builder("contraption_rotating", ContraptionProgram::new)
                                                                                     .addAttributes(ModelVertexAttributes.class)
                                                                                     .addAttributes(KineticVertexAttributes.class)
                                                                                     .addAttributes(RotatingVertexAttributes.class)
                                                                                     .setVert(Locations.ROTATING)
                                                                                     .setFrag(Locations.CONTRAPTION)
                                                                                     .setDefines(ShaderConstants.define("CONTRAPTION"))
                                                                                     .createProgramSpec());
    public static final ProgramSpec<ContraptionProgram> C_BELT = register(ProgramSpec.builder("contraption_belt", ContraptionProgram::new)
                                                                                 .addAttributes(ModelVertexAttributes.class)
                                                                                 .addAttributes(KineticVertexAttributes.class)
                                                                                 .addAttributes(BeltVertexAttributes.class)
                                                                                 .setVert(Locations.BELT)
                                                                                 .setFrag(Locations.CONTRAPTION)
                                                                                 .setDefines(ShaderConstants.define("CONTRAPTION"))
                                                                                 .createProgramSpec());
    public static final ProgramSpec<ContraptionProgram> C_FLAPS = register(ProgramSpec.builder("contraption_flap", ContraptionProgram::new)
                                                                                  .addAttributes(ModelVertexAttributes.class)
                                                                                  .addAttributes(FlapVertexAttributes.class)
                                                                                  .setVert(Locations.FLAP)
                                                                                  .setFrag(Locations.CONTRAPTION)
                                                                                  .setDefines(ShaderConstants.define("CONTRAPTION"))
                                                                                  .createProgramSpec());
    public static final ProgramSpec<ContraptionProgram> C_ACTOR = register(ProgramSpec.builder("contraption_actor", ContraptionProgram::new)
                                                                                  .addAttributes(ModelVertexAttributes.class)
                                                                                  .addAttributes(ActorVertexAttributes.class)
                                                                                  .setVert(Locations.CONTRAPTION_ACTOR)
                                                                                  .setFrag(Locations.CONTRAPTION)
                                                                                  .createProgramSpec());


    public static class Locations {
        public static final ResourceLocation MODEL_FRAG = loc("model.frag");
        public static final ResourceLocation MODEL_VERT = loc("model.vert");
        public static final ResourceLocation CONTRAPTION = loc("contraption.frag");

        public static final ResourceLocation ROTATING = loc("rotating.vert");
        public static final ResourceLocation BELT = loc("belt.vert");
        public static final ResourceLocation FLAP = loc("flap.vert");
        public static final ResourceLocation CONTRAPTION_STRUCTURE = loc("contraption_structure.vert");
        public static final ResourceLocation CONTRAPTION_ACTOR = loc("contraption_actor.vert");


        private static ResourceLocation loc(String name) {
            return new ResourceLocation(Create.ID, name);
        }
    }
}
