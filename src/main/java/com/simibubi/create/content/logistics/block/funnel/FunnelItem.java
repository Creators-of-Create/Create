package com.simibubi.create.content.logistics.block.funnel;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
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
		BlockState state = super.getStateForPlacement(ctx);
		if (state == null)
			return state;
		if (!(state.getBlock() instanceof FunnelBlock))
			return state;
		Direction direction = state.get(FunnelBlock.FACING);
		if (!direction.getAxis()
			.isHorizontal())
			return state;

		FunnelBlock block = (FunnelBlock) getBlock();
		Block beltFunnelBlock = block.getEquivalentBeltFunnel(state)
			.getBlock();
		Block chuteFunnelBlock = block.getEquivalentChuteFunnel(state)
			.getBlock();

		BlockState equivalentBeltFunnel = beltFunnelBlock.getStateForPlacement(ctx)
			.with(BeltFunnelBlock.HORIZONTAL_FACING, direction);
		BlockState equivalentChuteFunnel = chuteFunnelBlock.getStateForPlacement(ctx)
			.with(ChuteFunnelBlock.HORIZONTAL_FACING, direction);
		BlockState reversedChuteFunnel = equivalentChuteFunnel.rotate(Rotation.CLOCKWISE_180)
			.cycle(ChuteFunnelBlock.PUSHING);

		World world = ctx.getWorld();
		BlockPos pos = ctx.getPos();
		if (BeltFunnelBlock.isOnValidBelt(equivalentBeltFunnel, world, pos))
			return BeltFunnelBlock.updateShape(equivalentBeltFunnel, world, pos);
		if (ChuteFunnelBlock.isOnValidChute(equivalentChuteFunnel, world, pos))
			return equivalentChuteFunnel;
		if (ChuteFunnelBlock.isOnValidChute(reversedChuteFunnel, world, pos))
			return reversedChuteFunnel;

		return state;
	}

}
