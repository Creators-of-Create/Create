package com.simibubi.create.content.contraptions.itemAssembly;

import com.simibubi.create.foundation.utility.Color;

import com.simibubi.create.lib.item.CustomDurabilityBarItem;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SequencedAssemblyItem extends Item implements CustomDurabilityBarItem {

	public SequencedAssemblyItem(Properties p_i48487_1_) {
		super(p_i48487_1_.stacksTo(1));
	}

	@Override
	public void fillItemCategory(CreativeModeTab p_150895_1_, NonNullList<ItemStack> p_150895_2_) {}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		if (!stack.hasTag())
			return 1;
		CompoundTag tag = stack.getTag();
		if (!tag.contains("SequencedAssembly"))
			return 1;
		return Mth.lerp(tag.getCompound("SequencedAssembly")
			.getFloat("Progress"), 1, 0);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return Color.mixColors(0xFF_46FFE0, 0xFF_FFC074, (float) getDurabilityForDisplay(stack));
	}

}
