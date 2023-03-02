package com.simibubi.create.content.curiosities.frames;

import com.simibubi.create.content.palettes.GlassPaneBlock;

import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;

public class CopycatSpecialCases {

	public static boolean isBarsMaterial(BlockState material) {
		return material.getBlock() instanceof IronBarsBlock && !(material.getBlock() instanceof GlassPaneBlock)
			&& !(material.getBlock() instanceof StainedGlassPaneBlock);
	}

	public static boolean isTrapdoorMaterial(BlockState material) {
		return material.getBlock() instanceof TrapDoorBlock && material.hasProperty(TrapDoorBlock.HALF)
			&& material.hasProperty(TrapDoorBlock.OPEN) && material.hasProperty(TrapDoorBlock.FACING);
	}

}
