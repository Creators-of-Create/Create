package com.simibubi.create.compat.rei.display;

import com.simibubi.create.compat.rei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;

public class BlockCuttingDisplay extends AbstractCreateDisplay<CondensedBlockCuttingRecipe> {
	private BlockCuttingDisplay(CondensedBlockCuttingRecipe recipe, String id) {
		super(recipe, id);
	}

	public BlockCuttingDisplay(CondensedBlockCuttingRecipe recipe) {
		super(recipe, "block_cutting");
	}

	public static BlockCuttingDisplay woodCutting(CondensedBlockCuttingRecipe recipe) {
		return new BlockCuttingDisplay(recipe, "wood_cutting");
	}
}
