package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllTags;

import net.minecraft.data.DataGenerator;
import net.minecraft.item.Items;

public class FillingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe 
	
	HONEY_BOTTLE = create("honey_bottle", b -> b
		.require(AllTags.forgeFluidTag("honey"), 250)
		.require(Items.GLASS_BOTTLE)
		.output(Items.field_226638_pX_)),
	
	MILK_BUCKET = create("milk_bucket", b -> b
		.require(AllTags.forgeFluidTag("milk"), 1000)
		.require(Items.BUCKET)
		.output(Items.MILK_BUCKET))
	;

	public FillingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.FILLING;
	}

}
