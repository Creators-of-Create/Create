package com.simibubi.create.content.decoration.palettes;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ConnectedGlassPaneBlock extends GlassPaneBlock {

	public ConnectedGlassPaneBlock(Properties builder) {
		super(builder);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		if (side.getAxis()
			.isVertical())
			return adjacentBlockState == state;
		return super.skipRendering(state, adjacentBlockState, side);
	}

}
