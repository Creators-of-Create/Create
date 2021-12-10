package com.simibubi.create.foundation.render;

import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.flwdata.BeltData;
import com.simibubi.create.content.contraptions.base.flwdata.BeltType;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingType;
import com.simibubi.create.content.contraptions.components.actors.flwdata.ActorData;
import com.simibubi.create.content.contraptions.components.actors.flwdata.ActorType;
import com.simibubi.create.content.logistics.block.flap.FlapData;
import com.simibubi.create.content.logistics.block.flap.FlapType;

import net.minecraft.resources.ResourceLocation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class AllMaterialSpecs {
	public static void init() {
		// noop, make sure the static field are loaded.
	}

	public static final StructType<RotatingData> ROTATING = new RotatingType();
	public static final StructType<BeltData> BELTS = new BeltType();
	public static final StructType<ActorData> ACTORS = new ActorType();
	public static final StructType<FlapData> FLAPS = new FlapType();

	public static void flwInit(GatherContextEvent event) {
		event.getBackend().register(Locations.ROTATING, ROTATING);
		event.getBackend().register(Locations.BELTS, BELTS);
		event.getBackend().register(Locations.ACTORS, ACTORS);
		event.getBackend().register(Locations.FLAPS, FLAPS);
	}

	public static class Locations {
		public static final ResourceLocation ROTATING = Create.asResource("rotating");
		public static final ResourceLocation BELTS = Create.asResource("belts");
		public static final ResourceLocation ACTORS = Create.asResource("actors");
		public static final ResourceLocation FLAPS = Create.asResource("flaps");
	}
}
