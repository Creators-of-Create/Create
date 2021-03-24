package com.simibubi.create.foundation.render;

import static com.simibubi.create.foundation.render.backend.Backend.register;

import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticAttributes;
import com.simibubi.create.content.contraptions.base.RotatingAttributes;
import com.simibubi.create.content.contraptions.components.actors.ActorVertexAttributes;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionProgram;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionAttributes;
import com.simibubi.create.content.contraptions.relays.belt.BeltAttributes;
import com.simibubi.create.content.logistics.block.FlapAttributes;
import com.simibubi.create.foundation.render.backend.gl.BasicProgram;
import com.simibubi.create.foundation.render.backend.instancing.impl.BasicAttributes;
import com.simibubi.create.foundation.render.backend.instancing.impl.TransformAttributes;
import com.simibubi.create.foundation.render.backend.instancing.impl.ModelAttributes;
import com.simibubi.create.foundation.render.backend.instancing.impl.OrientedAttributes;
import com.simibubi.create.foundation.render.backend.gl.shader.ProgramSpec;
import com.simibubi.create.foundation.render.backend.gl.shader.ShaderConstants;

import net.minecraft.util.ResourceLocation;

public class AllProgramSpecs {
	public static void init() {
		// noop, make sure the static field are loaded.
	}

	public static final ProgramSpec<BasicProgram> MODEL = register(ProgramSpec.builder("model", BasicProgram::new)
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(TransformAttributes.class)
			.setVert(Locations.MODEL_VERT)
			.setFrag(Locations.MODEL_FRAG)
			.createProgramSpec());

	public static final ProgramSpec<BasicProgram> ORIENTED = register(ProgramSpec.builder("oriented", BasicProgram::new)
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(OrientedAttributes.class)
			.setVert(Locations.ORIENTED)
			.setFrag(Locations.MODEL_FRAG)
			.createProgramSpec());

	public static final ProgramSpec<BasicProgram> ROTATING = register(ProgramSpec.builder("rotating", BasicProgram::new)
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(KineticAttributes.class)
			.addAttributes(RotatingAttributes.class)
			.setVert(Locations.ROTATING)
			.setFrag(Locations.MODEL_FRAG)
			.createProgramSpec());

	public static final ProgramSpec<BasicProgram> BELT = register(ProgramSpec.builder("belt", BasicProgram::new)
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(KineticAttributes.class)
			.addAttributes(BeltAttributes.class)
			.setVert(Locations.BELT)
			.setFrag(Locations.MODEL_FRAG)
			.createProgramSpec());

	public static final ProgramSpec<BasicProgram> FLAPS = register(ProgramSpec.builder("flap", BasicProgram::new)
			.addAttributes(ModelAttributes.class)
			.addAttributes(FlapAttributes.class)
			.setVert(Locations.FLAP)
			.setFrag(Locations.MODEL_FRAG)
			.createProgramSpec());
	public static final ProgramSpec<ContraptionProgram> C_STRUCTURE = register(ProgramSpec.builder("contraption_structure", ContraptionProgram::new)
			.addAttributes(ContraptionAttributes.class)
			.setVert(Locations.CONTRAPTION_STRUCTURE)
			.setFrag(Locations.CONTRAPTION)
			.createProgramSpec());
	public static final ProgramSpec<ContraptionProgram> C_MODEL = register(ProgramSpec.builder("contraption_model", ContraptionProgram::new)
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(TransformAttributes.class)
			.setVert(Locations.MODEL_VERT)
			.setFrag(Locations.CONTRAPTION)
			.setDefines(ShaderConstants.define("CONTRAPTION"))
			.createProgramSpec());
	public static final ProgramSpec<ContraptionProgram> C_ORIENTED = register(ProgramSpec.builder("contraption_oriented", ContraptionProgram::new)
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(OrientedAttributes.class)
			.setVert(Locations.ORIENTED)
			.setFrag(Locations.CONTRAPTION)
			.setDefines(ShaderConstants.define("CONTRAPTION"))
			.createProgramSpec());
	public static final ProgramSpec<ContraptionProgram> C_ROTATING = register(ProgramSpec.builder("contraption_rotating", ContraptionProgram::new)
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(KineticAttributes.class)
			.addAttributes(RotatingAttributes.class)
			.setVert(Locations.ROTATING)
			.setFrag(Locations.CONTRAPTION)
			.setDefines(ShaderConstants.define("CONTRAPTION"))
			.createProgramSpec());
	public static final ProgramSpec<ContraptionProgram> C_BELT = register(ProgramSpec.builder("contraption_belt", ContraptionProgram::new)
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(KineticAttributes.class)
			.addAttributes(BeltAttributes.class)
			.setVert(Locations.BELT)
			.setFrag(Locations.CONTRAPTION)
			.setDefines(ShaderConstants.define("CONTRAPTION"))
			.createProgramSpec());
	public static final ProgramSpec<ContraptionProgram> C_FLAPS = register(ProgramSpec.builder("contraption_flap", ContraptionProgram::new)
			.addAttributes(ModelAttributes.class)
			.addAttributes(FlapAttributes.class)
			.setVert(Locations.FLAP)
			.setFrag(Locations.CONTRAPTION)
			.setDefines(ShaderConstants.define("CONTRAPTION"))
			.createProgramSpec());
	public static final ProgramSpec<ContraptionProgram> C_ACTOR = register(ProgramSpec.builder("contraption_actor", ContraptionProgram::new)
			.addAttributes(ModelAttributes.class)
			.addAttributes(ActorVertexAttributes.class)
			.setVert(Locations.CONTRAPTION_ACTOR)
			.setFrag(Locations.CONTRAPTION)
			.createProgramSpec());


	public static class Locations {
		public static final ResourceLocation MODEL_FRAG = loc("model.frag");
		public static final ResourceLocation MODEL_VERT = loc("model.vert");
		public static final ResourceLocation ORIENTED = loc("oriented.vert");
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
