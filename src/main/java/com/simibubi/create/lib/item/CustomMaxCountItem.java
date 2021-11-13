package com.simibubi.create.lib.item;

import net.minecraft.world.item.ItemStack;

public interface CustomMaxCountItem {
	int getItemStackLimit(ItemStack stack);
}
