package com.simibubi.create.foundation.block;

import net.minecraft.block.Block;
import net.minecraft.block.StairsBlock;

public class ProperStairsBlock extends StairsBlock {

	public ProperStairsBlock(Block block) {
		super(() -> block.getDefaultState(), Properties.from(block));
	}

}
