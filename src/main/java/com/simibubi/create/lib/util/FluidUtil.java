package com.simibubi.create.lib.util;

import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidUtil {
	public static int getLuminosity(Fluid fluid) {
		return fluid.defaultFluidState().createLegacyBlock().getLightEmission();
	}

	public static String getTranslationKey(Fluid fluid) {
		String translationKey;

		if (fluid == Fluids.EMPTY) {
			translationKey = "";
		} else if (fluid == Fluids.WATER) {
			translationKey = "block.minecraft.water";
		} else if (fluid == Fluids.LAVA) {
			translationKey = "block.minecraft.lava";
		} else {
			ResourceLocation id = Registry.FLUID.getKey(fluid);
			String key = Util.makeDescriptionId("block", id);
			String translated = I18n.get(key);
			translationKey = translated.equals(key) ? Util.makeDescriptionId("fluid", id) : key;
		}

		return translationKey;
	}
}
