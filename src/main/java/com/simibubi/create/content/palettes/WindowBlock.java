package com.simibubi.create.content.palettes;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class WindowBlock extends ConnectedGlassBlock {

	public WindowBlock(Properties p_i48392_1_) {
		super(p_i48392_1_);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		return adjacentBlockState.getBlock() instanceof ConnectedGlassBlock
			? (!ItemBlockRenderTypes.canRenderInLayer(state, RenderType.translucent()) && side.getAxis()
				.isHorizontal() || state.getBlock() == adjacentBlockState.getBlock())
			: super.skipRendering(state, adjacentBlockState, side);
	}

}
