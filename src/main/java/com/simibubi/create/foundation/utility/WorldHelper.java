package com.simibubi.create.foundation.utility;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;

public class WorldHelper {
	public static ResourceLocation getDimensionID(LevelAccessor world) {
		return world.registryAccess()
			.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
			.getKey(world.dimensionType());
	}
}
