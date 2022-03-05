package com.simibubi.create.content.logistics.block.funnel;

import com.simibubi.create.foundation.advancement.AllTriggers;
import io.github.fabricators_of_create.porting_lib.item.BlockUseBypassingItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.apache.logging.log4j.core.Filter.Result;

public class FunnelItem extends BlockItem implements BlockUseBypassingItem {

	public FunnelItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

//	public static void funnelItemAlwaysPlacesWhenUsed(PlayerInteractEvent.RightClickBlock event) {
//		if (event.getItemStack()
//				.getItem() instanceof FunnelItem)
//			event.setUseBlock(Result.DENY);
//	}

	@Override
	protected BlockState getPlacementState(BlockPlaceContext ctx) {
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState state = super.getPlacementState(ctx);
		if (state == null)
			return state;
		if (!(state.getBlock() instanceof FunnelBlock))
			return state;
		if (state.getValue(FunnelBlock.FACING)
			.getAxis()
			.isVertical())
			return state;

		Direction direction = state.getValue(FunnelBlock.FACING);
		FunnelBlock block = (FunnelBlock) getBlock();
		Block beltFunnelBlock = block.getEquivalentBeltFunnel(world, pos, state)
			.getBlock();
		BlockState equivalentBeltFunnel = beltFunnelBlock.getStateForPlacement(ctx)
			.setValue(BeltFunnelBlock.HORIZONTAL_FACING, direction);
		if (BeltFunnelBlock.isOnValidBelt(equivalentBeltFunnel, world, pos)) {
			AllTriggers.triggerFor(AllTriggers.BELT_FUNNEL, ctx.getPlayer());
			return equivalentBeltFunnel;
		}

		return state;
	}

	@Override
	public boolean shouldBypass(BlockState state, BlockPos pos, Level level, Player player, InteractionHand hand) {
		return true;
	}
}
