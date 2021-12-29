package com.simibubi.create.lib.util;

import net.minecraft.world.level.material.Fluid;

public class FluidUtil {
	public static int getLuminosity(Fluid fluid) {
		return fluid.defaultFluidState().createLegacyBlock().getLightEmission();
	}
}
