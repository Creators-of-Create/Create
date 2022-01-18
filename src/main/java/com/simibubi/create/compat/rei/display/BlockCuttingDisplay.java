package com.simibubi.create.compat.rei.display;

import com.simibubi.create.compat.rei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;

public class BlockCuttingDisplay extends CreateDisplay<CondensedBlockCuttingRecipe> {
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
