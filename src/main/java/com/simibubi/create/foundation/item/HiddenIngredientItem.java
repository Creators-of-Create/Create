package com.simibubi.create.foundation.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HiddenIngredientItem extends Item {

	public HiddenIngredientItem(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

	@Override
	public void fillItemCategory(CreativeModeTab p_150895_1_, NonNullList<ItemStack> p_150895_2_) {
		if (p_150895_1_ != CreativeModeTab.TAB_SEARCH)
			return;
		super.fillItemCategory(p_150895_1_, p_150895_2_);
	}

}
