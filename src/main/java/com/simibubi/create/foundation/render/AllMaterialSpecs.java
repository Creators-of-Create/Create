package com.simibubi.create.foundation.render;

import static com.jozufozu.flywheel.backend.Backend.register;

import com.jozufozu.flywheel.backend.core.ModelData;
import com.jozufozu.flywheel.backend.core.OrientedData;
import com.jozufozu.flywheel.backend.core.OrientedModel;
import com.jozufozu.flywheel.backend.core.TransformedModel;
import com.jozufozu.flywheel.backend.instancing.InstancedModel;
import com.jozufozu.flywheel.backend.instancing.MaterialSpec;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.RotatingData;
import com.simibubi.create.content.contraptions.base.RotatingModel;
import com.simibubi.create.content.contraptions.components.actors.ActorData;
import com.simibubi.create.content.contraptions.components.actors.ActorModel;
import com.simibubi.create.content.contraptions.relays.belt.BeltData;
import com.simibubi.create.content.contraptions.relays.belt.BeltInstancedModel;
import com.simibubi.create.content.logistics.block.FlapData;
import com.simibubi.create.content.logistics.block.FlapModel;

import net.minecraft.util.ResourceLocation;

public class AllMaterialSpecs {
	public static void init() {
		// noop, make sure the static field are loaded.
	}


	public static final MaterialSpec<InstancedModel<ModelData>> TRANSFORMED = register(new MaterialSpec<>(Locations.MODEL, AllProgramSpecs.MODEL, TransformedModel::new));
	public static final MaterialSpec<InstancedModel<OrientedData>> ORIENTED = register(new MaterialSpec<>(Locations.ORIENTED, AllProgramSpecs.ORIENTED, OrientedModel::new));

	public static final MaterialSpec<InstancedModel<RotatingData>> ROTATING = register(new MaterialSpec<>(Locations.ROTATING, AllProgramSpecs.ROTATING, RotatingModel::new));
	public static final MaterialSpec<InstancedModel<BeltData>> BELTS = register(new MaterialSpec<>(Locations.BELTS, AllProgramSpecs.BELT, BeltInstancedModel::new));
	public static final MaterialSpec<InstancedModel<ActorData>> ACTORS = register(new MaterialSpec<>(Locations.ACTORS, AllProgramSpecs.ACTOR, ActorModel::new));
	public static final MaterialSpec<InstancedModel<FlapData>> FLAPS = register(new MaterialSpec<>(Locations.FLAPS, AllProgramSpecs.FLAPS, FlapModel::new));

	public static class Locations {
		public static final ResourceLocation MODEL = new ResourceLocation("create", "model");
		public static final ResourceLocation ORIENTED = new ResourceLocation("create", "oriented");
		public static final ResourceLocation ROTATING = new ResourceLocation(Create.ID, "rotating");
		public static final ResourceLocation BELTS = new ResourceLocation(Create.ID, "belts");
		public static final ResourceLocation ACTORS = new ResourceLocation(Create.ID, "actors");
		public static final ResourceLocation FLAPS = new ResourceLocation(Create.ID, "flaps");
	}
}
