package com.simibubi.create.lib.mixin.common;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.simibubi.create.lib.item.CustomItemEnchantabilityItem;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {
	@ModifyVariable(method = "getEnchantmentCost", at = @At("STORE"), ordinal = 1, argsOnly = true)
	private static int create$calcItemStackEnchantability(int i, Random random, int slotIndex, int bookshelfCount, ItemStack stack) {
		return create$customItemEnchantment(i, stack);
	}

	@ModifyVariable(method = "selectEnchantment", at = @At("STORE"), ordinal = 0, argsOnly = true)
	private static int create$buildEnchantmentList(int i, Random random, ItemStack stack, int level, boolean treasureAllowed) {
		return create$customItemEnchantment(i, stack);
	}

	private static int create$customItemEnchantment(int i, ItemStack stack) {
		if (stack.getItem() instanceof CustomItemEnchantabilityItem) {
			int e = ((CustomItemEnchantabilityItem) stack.getItem()).getItemEnchantability(stack);
			if (e == 0) e = i;
			return e;
		}
		return i;
	}
}
