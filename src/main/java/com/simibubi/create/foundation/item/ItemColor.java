package com.simibubi.create.foundation.item;

import net.minecraft.world.item.ItemStack;

/**
 * Temporary Create 26.2 bridge for legacy item color callbacks.
 * TODO 26.2: Replace callers with ItemTintSource registrations.
 */
@Deprecated(forRemoval = true)
public interface ItemColor {
	int getColor(ItemStack stack, int layer);
}
