package com.simibubi.create.foundation.render;

import com.jozufozu.flywheel.backend.material.MaterialSpec;
import com.jozufozu.flywheel.core.Formats;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AllMaterialSpecs {
	public static void init() {
		// noop, make sure the static field are loaded.
	}

	public static final MaterialSpec<RotatingData> ROTATING = new MaterialSpec<>(Locations.ROTATING, AllProgramSpecs.ROTATING, Formats.UNLIT_MODEL, new RotatingType());
	public static final MaterialSpec<BeltData> BELTS = new MaterialSpec<>(Locations.BELTS, AllProgramSpecs.BELT, Formats.UNLIT_MODEL, new BeltType());
	public static final MaterialSpec<ActorData> ACTORS = new MaterialSpec<>(Locations.ACTORS, AllProgramSpecs.ACTOR, Formats.UNLIT_MODEL, new ActorType());
	public static final MaterialSpec<FlapData> FLAPS = new MaterialSpec<>(Locations.FLAPS, AllProgramSpecs.FLAPS, Formats.UNLIT_MODEL, new FlapType());

	public static void flwInit(GatherContextEvent event) {
		event.getBackend().register(ROTATING);
		event.getBackend().register(BELTS);
		event.getBackend().register(ACTORS);
		event.getBackend().register(FLAPS);
	}

	public static class Locations {
		public static final ResourceLocation ROTATING = Create.asResource("rotating");
		public static final ResourceLocation BELTS = Create.asResource("belts");
		public static final ResourceLocation ACTORS = Create.asResource("actors");
		public static final ResourceLocation FLAPS = Create.asResource("flaps");
	}
}
