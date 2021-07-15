package com.simibubi.create.foundation.utility;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;

public class WorldHelper {
	public static ResourceLocation getDimensionID(IWorld world) {
		return world.registryAccess()
			.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
			.getKey(world.dimensionType());
	}
}
