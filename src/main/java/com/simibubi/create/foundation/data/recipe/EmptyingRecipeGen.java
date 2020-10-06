package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllRecipeTypes;

import net.minecraft.data.DataGenerator;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraftforge.common.crafting.NBTIngredient;

public class EmptyingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	WATER_BOTTLE = create("water_bottle", b -> b
		.require(NBTIngredient.fromStacks(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER)))
		.output(Fluids.WATER, 250)
		.output(Items.GLASS_BOTTLE))

	;

	public EmptyingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.EMPTYING;
	}

}
