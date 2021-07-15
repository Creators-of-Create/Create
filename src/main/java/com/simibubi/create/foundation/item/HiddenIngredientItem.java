package com.simibubi.create.foundation.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import net.minecraft.item.Item.Properties;

public class HiddenIngredientItem extends Item {

	public HiddenIngredientItem(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

	@Override
	public void fillItemCategory(ItemGroup p_150895_1_, NonNullList<ItemStack> p_150895_2_) {
		if (p_150895_1_ != ItemGroup.TAB_SEARCH)
			return;
		super.fillItemCategory(p_150895_1_, p_150895_2_);
	}

}
