package com.simibubi.create.content.logistics.block.realityFunnel;

import com.simibubi.create.AllBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.Direction;

public class FunnelItem extends BlockItem {

	public FunnelItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	protected BlockState getStateForPlacement(BlockItemUseContext ctx) {
		BlockState state = super.getStateForPlacement(ctx);
		if (state == null)
			return state;
		if (!(state.getBlock() instanceof RealityFunnelBlock))
			return state;
		Direction direction = state.get(RealityFunnelBlock.FACING);
		if (!direction.getAxis()
			.isHorizontal())
			return state;
		BlockState equivalentBeltFunnel = AllBlocks.BELT_FUNNEL.get()
			.getStateForPlacement(ctx)
			.with(BeltFunnelBlock.HORIZONTAL_FACING, direction);
		if (BeltFunnelBlock.isOnValidBelt(equivalentBeltFunnel, ctx.getWorld(), ctx.getPos()))
			return equivalentBeltFunnel;
		return state;
	}

}
