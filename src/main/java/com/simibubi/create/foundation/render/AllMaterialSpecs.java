package com.simibubi.create.foundation.render;

import static com.jozufozu.flywheel.backend.Backend.register;

import com.jozufozu.flywheel.backend.core.materials.ModelData;
import com.jozufozu.flywheel.backend.core.materials.OrientedData;
import com.jozufozu.flywheel.backend.instancing.MaterialSpec;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.components.actors.ActorData;
import com.simibubi.create.content.contraptions.relays.belt.BeltData;
import com.simibubi.create.content.logistics.block.FlapData;

import net.minecraft.util.ResourceLocation;

public class AllMaterialSpecs {
	public static void init() {
		// noop, make sure the static field are loaded.
	}

	public static final MaterialSpec<ModelData> TRANSFORMED = register(new MaterialSpec<>(Locations.MODEL, AllProgramSpecs.MODEL, AllInstanceFormats.MODEL, ModelData::new));
	public static final MaterialSpec<OrientedData> ORIENTED = register(new MaterialSpec<>(Locations.ORIENTED, AllProgramSpecs.ORIENTED, AllInstanceFormats.ORIENTED, OrientedData::new));

	public static final MaterialSpec<RotatingData> ROTATING = register(new MaterialSpec<>(Locations.ROTATING, AllProgramSpecs.ROTATING, AllInstanceFormats.ROTATING, RotatingData::new));
	public static final MaterialSpec<BeltData> BELTS = register(new MaterialSpec<>(Locations.BELTS, AllProgramSpecs.BELT, AllInstanceFormats.BELT, BeltData::new));
	public static final MaterialSpec<ActorData> ACTORS = register(new MaterialSpec<>(Locations.ACTORS, AllProgramSpecs.ACTOR, AllInstanceFormats.ACTOR, ActorData::new));
	public static final MaterialSpec<FlapData> FLAPS = register(new MaterialSpec<>(Locations.FLAPS, AllProgramSpecs.FLAPS, AllInstanceFormats.FLAP, FlapData::new));

	public static class Locations {
		public static final ResourceLocation MODEL = new ResourceLocation("create", "model");
		public static final ResourceLocation ORIENTED = new ResourceLocation("create", "oriented");
		public static final ResourceLocation ROTATING = new ResourceLocation(Create.ID, "rotating");
		public static final ResourceLocation BELTS = new ResourceLocation(Create.ID, "belts");
		public static final ResourceLocation ACTORS = new ResourceLocation(Create.ID, "actors");
		public static final ResourceLocation FLAPS = new ResourceLocation(Create.ID, "flaps");
	}
}
