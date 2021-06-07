package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.instancing.InstanceData;
import com.jozufozu.flywheel.backend.instancing.MaterialSpec;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.jozufozu.flywheel.core.materials.OrientedData;
import com.jozufozu.flywheel.event.GatherContextEvent;
import com.simibubi.create.foundation.render.AllMaterialSpecs;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Materials {
	public static final MaterialSpec<OrientedData> ORIENTED = AllMaterialSpecs.register(new MaterialSpec<>(Locations.ORIENTED, Programs.ORIENTED, Formats.UNLIT_MODEL, Formats.ORIENTED, OrientedData::new));
	public static final MaterialSpec<ModelData> TRANSFORMED = AllMaterialSpecs.register(new MaterialSpec<>(Locations.MODEL, Programs.TRANSFORMED, Formats.UNLIT_MODEL, Formats.TRANSFORMED, ModelData::new));

	public static <D extends InstanceData> MaterialSpec<D> register(MaterialSpec<D> spec) {
		return Backend.getInstance().register(spec);
	}

	@SubscribeEvent
	public static void flwInit(GatherContextEvent event) {
		register(ORIENTED);
		register(TRANSFORMED);
	}

	public static class Locations {
		public static final ResourceLocation MODEL = new ResourceLocation("create", "model");
		public static final ResourceLocation ORIENTED = new ResourceLocation("create", "oriented");
	}
}
