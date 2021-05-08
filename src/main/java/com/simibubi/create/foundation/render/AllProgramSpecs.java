package com.simibubi.create.foundation.render;

import static com.jozufozu.flywheel.backend.Backend.register;

import com.jozufozu.flywheel.backend.core.materials.BasicAttributes;
import com.jozufozu.flywheel.backend.core.materials.ModelAttributes;
import com.jozufozu.flywheel.backend.core.materials.OrientedAttributes;
import com.jozufozu.flywheel.backend.core.materials.TransformAttributes;
import com.jozufozu.flywheel.backend.gl.shader.ProgramSpec;
import com.jozufozu.flywheel.backend.gl.shader.ShaderConstants;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.KineticAttributes;
import com.simibubi.create.content.contraptions.base.RotatingAttributes;
import com.simibubi.create.content.contraptions.components.actors.ActorVertexAttributes;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionAttributes;
import com.simibubi.create.content.contraptions.relays.belt.BeltAttributes;
import com.simibubi.create.content.logistics.block.FlapAttributes;

import net.minecraft.util.ResourceLocation;

public class AllProgramSpecs {
	public static void init() {
		// noop, make sure the static field are loaded.
	}

	public static final ProgramSpec CHROMATIC = register(builder("chromatic")
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(TransformAttributes.class)
			.setVert(Locations.EFFECT_VERT)
			.setFrag(Locations.EFFECT_FRAG)
			.createProgramSpec());

	public static final ProgramSpec MODEL = register(builder("model")
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(TransformAttributes.class)
			.setVert(Locations.MODEL_VERT)
			.setFrag(Locations.BLOCK)
			.createProgramSpec());

	public static final ProgramSpec ORIENTED = register(builder("oriented")
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(OrientedAttributes.class)
			.setVert(Locations.ORIENTED)
			.setFrag(Locations.BLOCK)
			.createProgramSpec());

	public static final ProgramSpec ROTATING = register(builder("rotating")
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(KineticAttributes.class)
			.addAttributes(RotatingAttributes.class)
			.setVert(Locations.ROTATING)
			.setFrag(Locations.BLOCK)
			.createProgramSpec());

	public static final ProgramSpec BELT = register(builder("belt")
			.addAttributes(ModelAttributes.class)
			.addAttributes(BasicAttributes.class)
			.addAttributes(KineticAttributes.class)
			.addAttributes(BeltAttributes.class)
			.setVert(Locations.BELT)
			.setFrag(Locations.BLOCK)
			.createProgramSpec());

	public static final ProgramSpec FLAPS = register(builder("flap")
			.addAttributes(ModelAttributes.class)
			.addAttributes(FlapAttributes.class)
			.setVert(Locations.FLAP)
			.setFrag(Locations.BLOCK)
			.createProgramSpec());
	public static final ProgramSpec STRUCTURE = register(builder("contraption_structure")
			.addAttributes(ContraptionAttributes.class)
			.setVert(Locations.CONTRAPTION_STRUCTURE)
			.setFrag(Locations.BLOCK)
			.setDefines(ShaderConstants.define("CONTRAPTION"))
			.createProgramSpec());
	public static final ProgramSpec ACTOR = register(builder("contraption_actor")
			.addAttributes(ModelAttributes.class)
			.addAttributes(ActorVertexAttributes.class)
			.setVert(Locations.CONTRAPTION_ACTOR)
			.setFrag(Locations.BLOCK)
			.setDefines(ShaderConstants.define("CONTRAPTION"))
			.createProgramSpec());

	public static ProgramSpec.Builder builder(String name) {
		return ProgramSpec.builder(new ResourceLocation(Create.ID, name));
	}

	public static class Locations {
		public static final ResourceLocation BLOCK = loc("block.frag");

		public static final ResourceLocation MODEL_VERT = loc("model.vert");
		public static final ResourceLocation ORIENTED = loc("oriented.vert");

		public static final ResourceLocation ROTATING = loc("rotating.vert");
		public static final ResourceLocation BELT = loc("belt.vert");
		public static final ResourceLocation FLAP = loc("flap.vert");
		public static final ResourceLocation CONTRAPTION_STRUCTURE = loc("contraption_structure.vert");
		public static final ResourceLocation CONTRAPTION_ACTOR = loc("contraption_actor.vert");

		public static final ResourceLocation EFFECT_VERT = loc("area_effect.vert");
		public static final ResourceLocation EFFECT_FRAG = loc("area_effect.frag");

		private static ResourceLocation loc(String name) {
			return new ResourceLocation(Create.ID, name);
		}
	}
}
