package com.simibubi.create.lib.helper;

import com.simibubi.create.lib.mixin.accessor.ItemInHandRendererAccessor;

import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;

public class FirstPersonRendererHelper {
	public static ItemStack getStackInMainHand(ItemInHandRenderer renderer) {
		return ((ItemInHandRendererAccessor) renderer).getItemStackMainHand();
	}

	public static ItemStack getStackInOffHand(ItemInHandRenderer renderer) {
		return ((ItemInHandRendererAccessor) renderer).getItemStackOffHand();
	}
}
