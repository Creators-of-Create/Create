package com.simibubi.create.foundation.utility;

import net.minecraft.block.Block;

public class VoxelShapers {

	public static final VoxelShaper SHORT_CASING = VoxelShaper
			.forDirectional(Block.makeCuboidShape(0, 0, 0, 16, 16, 12));

}
