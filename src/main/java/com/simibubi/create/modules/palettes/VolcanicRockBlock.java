package com.simibubi.create.modules.palettes;

import com.simibubi.create.foundation.block.IHaveColoredVertices;
import com.simibubi.create.foundation.utility.ColorHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class VolcanicRockBlock extends Block implements IHaveColoredVertices {

	public VolcanicRockBlock() {
		super(Properties.from(Blocks.ANDESITE));
	}

	@Override
	public MaterialColor getMaterialColor(BlockState state, IBlockReader worldIn, BlockPos pos) {
		return MaterialColor.GRAY_TERRACOTTA;
	}

	@Override
	public int getColor(float x, float y, float z) {
		float x2 = (float) Math.floor(z + x - y * .5);
		float y2 = (float) Math.floor(y * 1.5 + x * .5 - z);
		float z2 = (float) Math.floor(y - z * .5 - x);

		int color = 0x448888;
		if (x2 % 2 == 0)
			color |= 0x0011ff;
		if (z2 % 2 == 0)
			color |= 0x888888;
		color = ColorHelper.mixColors(ColorHelper.rainbowColor((int) (x + y + z) * 170), color, .6f);
		if ((x2 % 4 == 0) || (y2 % 4 == 0))
			color = ColorHelper.mixColors(0xffffff, color, .2f);
		return color;
	}

}
