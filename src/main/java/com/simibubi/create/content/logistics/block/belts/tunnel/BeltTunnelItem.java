package com.simibubi.create.content.logistics.block.belts.tunnel;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.belt.BeltHelper;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity.CasingType;
import com.simibubi.create.foundation.advancement.AllTriggers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;

public class BeltTunnelItem extends BlockItem {

	public BeltTunnelItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	protected boolean canPlace(BlockItemUseContext ctx, BlockState state) {
		PlayerEntity playerentity = ctx.getPlayer();
		ISelectionContext iselectioncontext =
			playerentity == null ? ISelectionContext.empty() : ISelectionContext.of(playerentity);
		World world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		return (!this.mustSurvive() || AllBlocks.ANDESITE_TUNNEL.get()
			.isValidPositionForPlacement(state, world, pos)) && world.isUnobstructed(state, pos, iselectioncontext);
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, World world, PlayerEntity p_195943_3_, ItemStack p_195943_4_,
		BlockState state) {
		boolean flag = super.updateCustomBlockEntityTag(pos, world, p_195943_3_, p_195943_4_, state);
		if (!world.isClientSide) {
			BeltTileEntity belt = BeltHelper.getSegmentTE(world, pos.below());
			if (belt != null) {
				AllTriggers.triggerFor(AllTriggers.PLACE_TUNNEL, p_195943_3_);
				if (belt.casing == CasingType.NONE)
					belt.setCasingType(AllBlocks.ANDESITE_TUNNEL.has(state) ? CasingType.ANDESITE : CasingType.BRASS);
			}
		}
		return flag;
	}

}
