package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllRecipeTypes;

import net.minecraft.data.DataGenerator;
import net.minecraft.item.Items;

public class EmptyingRecipeGen extends ProcessingRecipeGen {

	/*
	 * potion/water bottles are handled internally
	 */
	
	GeneratedRecipe

	HONEY_BOTTLE = create("honey_bottle", b -> b
		.require(Items.field_226638_pX_)
		.output(AllFluids.HONEY.get(), 250)
		.output(Items.GLASS_BOTTLE)),
	
	MILK_BUCKET = create("milk_bucket", b -> b
		.require(Items.MILK_BUCKET)
		.output(AllFluids.MILK.get(), 1000)
		.output(Items.BUCKET))

	;

	public EmptyingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.EMPTYING;
	}

}
