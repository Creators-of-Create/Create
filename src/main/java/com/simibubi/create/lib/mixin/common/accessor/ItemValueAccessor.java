package com.simibubi.create.lib.mixin.common.accessor;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Ingredient.ItemValue.class)
public interface ItemValueAccessor {
	@Invoker("<init>")
	static Ingredient.ItemValue createItemValue(ItemStack itemStack) {
		throw new UnsupportedOperationException();
	}
}
