package com.simibubi.create.foundation.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

public class EnchantedBookItem {
	private EnchantedBookItem() {}

	public static ItemStack createForEnchantment(EnchantmentInstance enchantment) {
		ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
		EnchantmentHelper.updateEnchantments(stack, mutable -> mutable.set(enchantment.enchantment(), enchantment.level()));
		return stack;
	}
}
