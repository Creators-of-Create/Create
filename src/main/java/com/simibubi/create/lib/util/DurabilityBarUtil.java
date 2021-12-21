package com.simibubi.create.lib.util;

import com.simibubi.create.lib.item.CustomDurabilityBarItem;

import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class DurabilityBarUtil {
	public static boolean showDurabilityBarDefault(ItemStack stack) {
		return stack.isDamaged();
	}

	public static double getDurabilityForDisplayDefault(ItemStack stack) {
		return stack.getDamageValue() / stack.getMaxDamage();
	}

	public static int getRGBDurabilityForDisplayDefault(ItemStack stack) {
		return Mth.hsvToRgb((float) Math.max(0.0D, 1.0D - getDurabilityForDisplayDefault(stack)) / 3.0F, 1.0F, 1.0F);
	}

	public static double getDurabilityForDisplay(ItemStack stack) {
		Item item = stack.getItem();
		if (item instanceof CustomDurabilityBarItem) {
			return ((CustomDurabilityBarItem) item).getDurabilityForDisplay(stack);
		}
		return getDurabilityForDisplayDefault(stack);
	}

	private DurabilityBarUtil() {}
}
