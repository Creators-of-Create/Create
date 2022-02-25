package com.simibubi.create.lib.item;

import net.minecraft.world.item.ItemStack;

public interface ReequipAnimationItem {
	boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged);
}
