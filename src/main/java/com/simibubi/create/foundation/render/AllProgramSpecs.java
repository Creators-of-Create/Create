package com.simibubi.create.foundation.render;

import static com.jozufozu.flywheel.backend.Backend.register;

import com.jozufozu.flywheel.backend.gl.shader.ProgramSpec;
import com.jozufozu.flywheel.backend.gl.shader.ShaderConstants;
import com.simibubi.create.Create;

import net.minecraft.util.ResourceLocation;

public class AllProgramSpecs {
	public static void init() {
		// noop, make sure the static field are loaded.
	}

	public static final ProgramSpec CHROMATIC = register(builder("chromatic")
			.setVert(Locations.EFFECT_VERT)
			.setFrag(Locations.EFFECT_FRAG)
			.build());

	public static final ProgramSpec MODEL = register(builder("model")
			.setVert(Locations.MODEL_VERT)
			.setFrag(Locations.BLOCK)
			.build());

	public static final ProgramSpec ORIENTED = register(builder("oriented")
			.setVert(Locations.ORIENTED)
			.setFrag(Locations.BLOCK)
			.build());

	public static final ProgramSpec ROTATING = register(builder("rotating")
			.setVert(Locations.ROTATING)
			.setFrag(Locations.BLOCK)
			.build());

	public static final ProgramSpec BELT = register(builder("belt")
			.setVert(Locations.BELT)
			.setFrag(Locations.BLOCK)
			.build());

	public static final ProgramSpec FLAPS = register(builder("flap")
			.setVert(Locations.FLAP)
			.setFrag(Locations.BLOCK)
			.build());
	public static final ProgramSpec STRUCTURE = register(builder("contraption_structure")
			.setVert(Locations.CONTRAPTION_STRUCTURE)
			.setFrag(Locations.BLOCK)
			.setDefines(ShaderConstants.define("CONTRAPTION"))
			.build());
	public static final ProgramSpec ACTOR = register(builder("contraption_actor")
			.setVert(Locations.CONTRAPTION_ACTOR)
			.setFrag(Locations.BLOCK)
			.setDefines(ShaderConstants.define("CONTRAPTION"))
			.build());

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
