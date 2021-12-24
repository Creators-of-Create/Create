package com.simibubi.create.lib.util;

import com.simibubi.create.lib.mixin.accessor.BiomeManagerAccessor;

import net.minecraft.world.level.biome.BiomeManager;

public final class BiomeManagerHelper {
	public static long getSeed(BiomeManager biomeManager) {
		return get(biomeManager).create$getBiomeZoomSeed();
	}

	private static BiomeManagerAccessor get(BiomeManager biomeManager) {
		return MixinHelper.cast(biomeManager);
	}

	private BiomeManagerHelper() {}
}
