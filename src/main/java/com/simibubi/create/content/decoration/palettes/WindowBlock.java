package com.simibubi.create.content.decoration.palettes;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
