package com.simibubi.create.foundation.render;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.LightLayer;

/**
 * Temporary Create 26.2 bridge for call sites that used LevelRenderer#getLightColor.
 * TODO 26.2: Replace with the official client render-state lighting utility once settled.
 */
@Deprecated(forRemoval = true)
public final class LegacyLightTexture {
	public static final int FULL_BRIGHT = 0x00F000F0;

	private LegacyLightTexture() {}

	public static int getLightColor(Object level, BlockPos pos) {
		if (!(level instanceof BlockAndLightGetter lightGetter))
			return FULL_BRIGHT;
		int block = lightGetter.getBrightness(LightLayer.BLOCK, pos);
		int sky = lightGetter.getBrightness(LightLayer.SKY, pos);
		return block << 4 | sky << 20;
	}
}
