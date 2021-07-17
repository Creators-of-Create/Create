package com.simibubi.create.content.contraptions.itemAssembly;

import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;

import net.minecraft.item.Item.Properties;

public class SequencedAssemblyItem extends Item {

	public SequencedAssemblyItem(Properties p_i48487_1_) {
		super(p_i48487_1_.stacksTo(1));
	}

	@Override
	public void fillItemCategory(ItemGroup pGroup, NonNullList<ItemStack> pItems) {}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		if (!stack.hasTag())
			return 1;
		CompoundNBT tag = stack.getTag();
		if (!tag.contains("SequencedAssembly"))
			return 1;
		return MathHelper.lerp(tag.getCompound("SequencedAssembly")
			.getFloat("Progress"), 1, 0);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return ColorHelper.mixColors(0xFF_46FFE0, 0xFF_FFC074, (float) getDurabilityForDisplay(stack));
	}

}
