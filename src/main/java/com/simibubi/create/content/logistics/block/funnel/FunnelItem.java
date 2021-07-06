package com.simibubi.create.content.logistics.block.funnel;

import com.simibubi.create.foundation.advancement.AllTriggers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
	protected BlockState getStateForPlacement(BlockItemUseContext ctx) {
		World world = ctx.getWorld();
		BlockPos pos = ctx.getPos();
		BlockState state = super.getStateForPlacement(ctx);
		if (state == null)
			return state;
		if (!(state.getBlock() instanceof FunnelBlock))
			return state;
		if (state.get(FunnelBlock.FACING)
			.getAxis()
			.isVertical())
			return state;

		Direction direction = state.get(FunnelBlock.FACING);
		FunnelBlock block = (FunnelBlock) getBlock();
		Block beltFunnelBlock = block.getEquivalentBeltFunnel(world, pos, state)
			.getBlock();
		BlockState equivalentBeltFunnel = beltFunnelBlock.getStateForPlacement(ctx)
			.with(BeltFunnelBlock.HORIZONTAL_FACING, direction);
		if (BeltFunnelBlock.isOnValidBelt(equivalentBeltFunnel, world, pos)) {
			AllTriggers.triggerFor(AllTriggers.BELT_FUNNEL, ctx.getPlayer());
			return equivalentBeltFunnel;
		}

		return state;
	}

}
