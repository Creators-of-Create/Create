package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllRecipeTypes;

import net.minecraft.data.DataGenerator;

public class EmptyingRecipeGen extends ProcessingRecipeGen {

	/*
	 * potion/bottles are handled internally now. keeping this builder for reference
	 */
	
//	GeneratedRecipe
//
//	WATER_BOTTLE = create("water_bottle", b -> b
//		.require(NBTIngredient.fromStacks(PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER)))
//		.output(Fluids.WATER, 250)
//		.output(Items.GLASS_BOTTLE))
//
//	;

	public EmptyingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.EMPTYING;
	}

}
