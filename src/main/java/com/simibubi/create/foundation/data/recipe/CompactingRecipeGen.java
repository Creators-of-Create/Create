package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.palettes.AllPaletteBlocks;

import net.minecraft.data.DataGenerator;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Items;
import net.minecraft.tags.FluidTags;

public class CompactingRecipeGen extends ProcessingRecipeGen {

	GeneratedRecipe

	TEMPGABBRO = create("temp_gabbro", b -> b
		.require(Items.COBBLESTONE)
		.require(FluidTags.LAVA, 250)
		.output(AllPaletteBlocks.GABBRO.get(), 1)),
	
	ICE = create("ice", b -> b
		.require(Items.ICE)
		.output(Fluids.WATER, 250))

	;

	public CompactingRecipeGen(DataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.COMPACTING;
	}

}
