package com.simibubi.create.lib.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface EntitySwingListenerItem {
	boolean onEntitySwing(ItemStack stack, LivingEntity entity);
}
