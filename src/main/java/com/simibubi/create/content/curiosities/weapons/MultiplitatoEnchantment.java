package com.simibubi.create.content.curiosities.weapons;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class MultiplitatoEnchantment extends Enchantment {

	public MultiplitatoEnchantment(Rarity p_i46731_1_, EnchantmentCategory p_i46731_2_, EquipmentSlot[] p_i46731_3_) {
		super(p_i46731_1_, p_i46731_2_, p_i46731_3_);
	}

	@Override
	public int getMaxLevel() {
		return 2;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return false;
	}

}
