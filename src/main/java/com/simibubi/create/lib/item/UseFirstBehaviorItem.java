package com.simibubi.create.lib.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public interface UseFirstBehaviorItem {
	InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context);
}
