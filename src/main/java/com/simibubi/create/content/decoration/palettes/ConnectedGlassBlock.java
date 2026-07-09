package com.simibubi.create.content.decoration.palettes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.api.distmarker.Dist;

public class ConnectedGlassBlock extends TransparentBlock {

	public ConnectedGlassBlock(Properties p_i48392_1_) {
		super(p_i48392_1_);
	}

	@Override
	public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		return adjacentBlockState.getBlock() instanceof ConnectedGlassBlock || super.skipRendering(state, adjacentBlockState, side);
	}

	public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter world, BlockPos pos, FluidState fluidState) {
		return true;
	}
}
