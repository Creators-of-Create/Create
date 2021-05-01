package com.simibubi.create.foundation.render;

import static com.jozufozu.flywheel.backend.Backend.register;

import com.jozufozu.flywheel.backend.core.BasicAttributes;
import com.jozufozu.flywheel.backend.core.BasicProgram;
import com.jozufozu.flywheel.backend.core.ModelAttributes;
import com.jozufozu.flywheel.backend.core.OrientedAttributes;
import com.jozufozu.flywheel.backend.core.TransformAttributes;
import com.jozufozu.flywheel.backend.effects.SphereFilterProgram;
import com.jozufozu.flywheel.backend.gl.shader.FogSensitiveProgram;
import com.jozufozu.flywheel.backend.gl.shader.ProgramSpec;
import com.jozufozu.flywheel.backend.gl.shader.ShaderConstants;
import com.jozufozu.flywheel.backend.gl.shader.SingleProgram;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticAttributes;
import com.simibubi.create.content.contraptions.base.RotatingAttributes;
import com.simibubi.create.content.contraptions.components.actors.ActorVertexAttributes;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionAttributes;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionProgram;
import com.simibubi.create.content.contraptions.relays.belt.BeltAttributes;
import com.simibubi.create.content.logistics.block.FlapAttributes;

import net.minecraft.util.ResourceLocation;

public class AllProgramSpecs {
	public static void init() {
		// noop, make sure the static field are loaded.
	}

	public static final ProgramSpec<SphereFilterProgram> CHROMATIC = register(ProgramSpec.builder("chromatic", new SingleProgram.SpecLoader<>(SphereFilterProgram::new))
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(TransformAttributes.class)
			.setVert(Locations.EFFECT_VERT)
			.setFrag(Locations.EFFECT_FRAG)
			.createProgramSpec());

	public static final ProgramSpec<BasicProgram> MODEL = register(ProgramSpec.builder("model", new FogSensitiveProgram.SpecLoader<>(BasicProgram::new))
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(TransformAttributes.class)
			.setVert(Locations.MODEL_VERT)
			.setFrag(Locations.MODEL_FRAG)
			.createProgramSpec());

	public static final ProgramSpec<BasicProgram> ORIENTED = register(ProgramSpec.builder("oriented", new FogSensitiveProgram.SpecLoader<>(BasicProgram::new))
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(OrientedAttributes.class)
			.setVert(Locations.ORIENTED)
			.setFrag(Locations.MODEL_FRAG)
			.createProgramSpec());

	public static final ProgramSpec<BasicProgram> ROTATING = register(ProgramSpec.builder("rotating", new FogSensitiveProgram.SpecLoader<>(BasicProgram::new))
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(KineticAttributes.class)
			.addAttributes(RotatingAttributes.class)
			.setVert(Locations.ROTATING)
			.setFrag(Locations.MODEL_FRAG)
			.createProgramSpec());

	public static final ProgramSpec<BasicProgram> BELT = register(ProgramSpec.builder("belt", new FogSensitiveProgram.SpecLoader<>(BasicProgram::new))
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(KineticAttributes.class)
			.addAttributes(BeltAttributes.class)
			.setVert(Locations.BELT)
			.setFrag(Locations.MODEL_FRAG)
			.createProgramSpec());

	public static final ProgramSpec<BasicProgram> FLAPS = register(ProgramSpec.builder("flap", new FogSensitiveProgram.SpecLoader<>(BasicProgram::new))
			.addAttributes(ModelAttributes.class)
			.addAttributes(FlapAttributes.class)
			.setVert(Locations.FLAP)
			.setFrag(Locations.MODEL_FRAG)
			.createProgramSpec());
	public static final ProgramSpec<ContraptionProgram> C_STRUCTURE = register(ProgramSpec.builder("contraption_structure", new FogSensitiveProgram.SpecLoader<>(ContraptionProgram::new))
			.addAttributes(ContraptionAttributes.class)
			.setVert(Locations.CONTRAPTION_STRUCTURE)
			.setFrag(Locations.CONTRAPTION)
			.createProgramSpec());
	public static final ProgramSpec<ContraptionProgram> C_MODEL = register(ProgramSpec.builder("contraption_model", new FogSensitiveProgram.SpecLoader<>(ContraptionProgram::new))
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(TransformAttributes.class)
			.setVert(Locations.MODEL_VERT)
			.setFrag(Locations.CONTRAPTION)
			.setDefines(ShaderConstants.define("CONTRAPTION"))
			.createProgramSpec());
	public static final ProgramSpec<ContraptionProgram> C_ORIENTED = register(ProgramSpec.builder("contraption_oriented", new FogSensitiveProgram.SpecLoader<>(ContraptionProgram::new))
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(OrientedAttributes.class)
			.setVert(Locations.ORIENTED)
			.setFrag(Locations.CONTRAPTION)
			.setDefines(ShaderConstants.define("CONTRAPTION"))
			.createProgramSpec());
	public static final ProgramSpec<ContraptionProgram> C_ROTATING = register(ProgramSpec.builder("contraption_rotating", new FogSensitiveProgram.SpecLoader<>(ContraptionProgram::new))
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(KineticAttributes.class)
			.addAttributes(RotatingAttributes.class)
			.setVert(Locations.ROTATING)
			.setFrag(Locations.CONTRAPTION)
			.setDefines(ShaderConstants.define("CONTRAPTION"))
			.createProgramSpec());
	public static final ProgramSpec<ContraptionProgram> C_BELT = register(ProgramSpec.builder("contraption_belt", new FogSensitiveProgram.SpecLoader<>(ContraptionProgram::new))
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(KineticAttributes.class)
			.addAttributes(BeltAttributes.class)
			.setVert(Locations.BELT)
			.setFrag(Locations.CONTRAPTION)
			.setDefines(ShaderConstants.define("CONTRAPTION"))
			.createProgramSpec());
	public static final ProgramSpec<ContraptionProgram> C_FLAPS = register(ProgramSpec.builder("contraption_flap", new FogSensitiveProgram.SpecLoader<>(ContraptionProgram::new))
			.addAttributes(ModelAttributes.class)
			.addAttributes(FlapAttributes.class)
			.setVert(Locations.FLAP)
			.setFrag(Locations.CONTRAPTION)
			.setDefines(ShaderConstants.define("CONTRAPTION"))
			.createProgramSpec());
	public static final ProgramSpec<ContraptionProgram> C_ACTOR = register(ProgramSpec.builder("contraption_actor", new FogSensitiveProgram.SpecLoader<>(ContraptionProgram::new))
			.addAttributes(ModelAttributes.class)
			.addAttributes(ActorVertexAttributes.class)
			.setVert(Locations.CONTRAPTION_ACTOR)
			.setFrag(Locations.CONTRAPTION)
			.createProgramSpec());


	public static class Locations {
		public static final ResourceLocation EFFECT_VERT = loc("area_effect.vert");
		public static final ResourceLocation EFFECT_FRAG = loc("area_effect.frag");
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
