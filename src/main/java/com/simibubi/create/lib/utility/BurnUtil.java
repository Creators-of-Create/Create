package com.simibubi.create.lib.utility;

import com.simibubi.create.AllItems;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.world.item.ItemStack;

public class BurnUtil {
	public static int getBurnTime(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		} else if (stack.is(AllItems.CREATIVE_BLAZE_CAKE.get())) {
			return Integer.MAX_VALUE;
		}else {
			Integer burnTime = FuelRegistry.INSTANCE.get(stack.getItem());
			return burnTime == null ? 0 : burnTime;
		}
	}
}
