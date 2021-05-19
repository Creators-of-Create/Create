package com.simibubi.create.foundation.render;

import static com.jozufozu.flywheel.backend.Backend.register;

import com.jozufozu.flywheel.Flywheel;
import com.jozufozu.flywheel.backend.gl.shader.ProgramSpec;
import com.simibubi.create.Create;

import net.minecraft.util.ResourceLocation;

public class AllProgramSpecs {
	public static void init() {
		// noop, make sure the static field are loaded.
	}

	public static final ProgramSpec CHROMATIC = register(new ProgramSpec(loc("chromatic"), Locations.EFFECT_VERT, Locations.EFFECT_FRAG));
	public static final ProgramSpec MODEL = register(new ProgramSpec(new ResourceLocation(Flywheel.ID, "model"), Locations.MODEL_VERT, Locations.BLOCK));
	public static final ProgramSpec ORIENTED = register(new ProgramSpec(new ResourceLocation(Flywheel.ID, "oriented"), Locations.ORIENTED, Locations.BLOCK));
	public static final ProgramSpec ROTATING = register(new ProgramSpec(loc("rotating"), Locations.ROTATING, Locations.BLOCK));
	public static final ProgramSpec BELT = register(new ProgramSpec(loc("belt"), Locations.BELT, Locations.BLOCK));
	public static final ProgramSpec FLAPS = register(new ProgramSpec(loc("flap"), Locations.FLAP, Locations.BLOCK));
	public static final ProgramSpec STRUCTURE = register(new ProgramSpec(loc("contraption_structure"), Locations.CONTRAPTION_STRUCTURE, Locations.BLOCK));
	public static final ProgramSpec ACTOR = register(new ProgramSpec(loc("contraption_actor"), Locations.CONTRAPTION_ACTOR, Locations.BLOCK));

	public static class Locations {
		public static final ResourceLocation BLOCK = new ResourceLocation(Flywheel.ID, "block.frag");

		public static final ResourceLocation MODEL_VERT = new ResourceLocation(Flywheel.ID, "model.vert");
		public static final ResourceLocation ORIENTED = new ResourceLocation(Flywheel.ID, "oriented.vert");

		public static final ResourceLocation ROTATING = loc("rotating.vert");
		public static final ResourceLocation BELT = loc("belt.vert");
		public static final ResourceLocation FLAP = loc("flap.vert");
		public static final ResourceLocation CONTRAPTION_STRUCTURE = loc("contraption_structure.vert");
		public static final ResourceLocation CONTRAPTION_ACTOR = loc("contraption_actor.vert");

		public static final ResourceLocation EFFECT_VERT = loc("area_effect.vert");
		public static final ResourceLocation EFFECT_FRAG = loc("area_effect.frag");
	}

	private static ResourceLocation loc(String name) {
		return new ResourceLocation(Create.ID, name);
	}
}
