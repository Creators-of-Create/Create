package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;

import net.minecraft.data.DataGenerator;
import net.minecraft.item.Items;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.common.Tags;

public class CompactingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe
	
	BLAZE_CAKE = create("blaze_cake", b -> b
		.require(Tags.Items.EGGS)
		.require(Items.SUGAR)
		.require(AllItems.CINDER_FLOUR.get())
		.require(FluidTags.LAVA, 125)
		.output(AllItems.BLAZE_CAKE.get(), 1))
	
	;

	public CompactingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.COMPACTING;
	}

}
