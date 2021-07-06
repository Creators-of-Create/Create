package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllRecipeTypes;

import net.minecraft.data.DataGenerator;

public class DeployingRecipeGen extends ProcessingRecipeGen {

//	GeneratedRecipe
//	TEST = create("test", b -> b.require(AllItems.ANDESITE_ALLOY.get())
//		.require(AllItems.BAR_OF_CHOCOLATE.get())
//		.output(AllItems.BRASS_NUGGET.get())),
//	;

	public DeployingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.DEPLOYING;
	}

}
