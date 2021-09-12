package com.simibubi.create.foundation.render;

import com.jozufozu.flywheel.backend.material.MaterialSpec;
import com.jozufozu.flywheel.backend.struct.BasicStructType;
import com.jozufozu.flywheel.backend.struct.StructType;
import com.jozufozu.flywheel.core.Formats;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.components.actors.ActorData;
import com.simibubi.create.content.contraptions.relays.belt.BeltData;
import com.simibubi.create.content.logistics.block.FlapData;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AllMaterialSpecs {
	public static void init() {
		// noop, make sure the static field are loaded.
	}

	public static final StructType<RotatingData> ROTATING_TYPE = new BasicStructType<>(RotatingData::new, AllInstanceFormats.ROTATING);
	public static final StructType<BeltData> BELTS_TYPE = new BasicStructType<>(BeltData::new, AllInstanceFormats.BELT);
	public static final StructType<ActorData> ACTORS_TYPE = new BasicStructType<>(ActorData::new, AllInstanceFormats.ACTOR);
	public static final StructType<FlapData> FLAPS_TYPE = new BasicStructType<>(FlapData::new, AllInstanceFormats.FLAP);

	public static final MaterialSpec<RotatingData> ROTATING = new MaterialSpec<>(Locations.ROTATING, AllProgramSpecs.ROTATING, Formats.UNLIT_MODEL, ROTATING_TYPE);
	public static final MaterialSpec<BeltData> BELTS = new MaterialSpec<>(Locations.BELTS, AllProgramSpecs.BELT, Formats.UNLIT_MODEL, BELTS_TYPE);
	public static final MaterialSpec<ActorData> ACTORS = new MaterialSpec<>(Locations.ACTORS, AllProgramSpecs.ACTOR, Formats.UNLIT_MODEL, ACTORS_TYPE);
	public static final MaterialSpec<FlapData> FLAPS = new MaterialSpec<>(Locations.FLAPS, AllProgramSpecs.FLAPS, Formats.UNLIT_MODEL, FLAPS_TYPE);

	public static void flwInit(GatherContextEvent event) {
		event.getBackend().register(ROTATING);
		event.getBackend().register(BELTS);
		event.getBackend().register(ACTORS);
		event.getBackend().register(FLAPS);
	}

	public static class Locations {
		public static final ResourceLocation ROTATING = new ResourceLocation(Create.ID, "rotating");
		public static final ResourceLocation BELTS = new ResourceLocation(Create.ID, "belts");
		public static final ResourceLocation ACTORS = new ResourceLocation(Create.ID, "actors");
		public static final ResourceLocation FLAPS = new ResourceLocation(Create.ID, "flaps");
	}
}
