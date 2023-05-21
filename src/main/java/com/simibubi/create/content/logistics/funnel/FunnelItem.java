package com.simibubi.create.content.logistics.funnel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class FunnelItem extends BlockItem {

	public FunnelItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@SubscribeEvent
	public static void funnelItemAlwaysPlacesWhenUsed(PlayerInteractEvent.RightClickBlock event) {
		if (event.getItemStack()
			.getItem() instanceof FunnelItem)
			event.setUseBlock(Result.DENY);
	}

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
		if (BeltFunnelBlock.isOnValidBelt(equivalentBeltFunnel, world, pos)) 
			return equivalentBeltFunnel;

		return state;
	}

}
