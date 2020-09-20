package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllRecipeTypes;

import net.minecraft.data.DataGenerator;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tags.FluidTags;

public class FillingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	WATER_BOTTLE = create("water_bottle", b -> b.require(Items.GLASS_BOTTLE)
		.require(FluidTags.WATER, 250)
		.output(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER)))

	;

	public FillingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.FILLING;
	}

}
