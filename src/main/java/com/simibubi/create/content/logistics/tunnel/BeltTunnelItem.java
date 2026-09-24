package com.simibubi.create.content.logistics.tunnel;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.belt.AllBeltCasingTypes;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltCasingType;
import com.simibubi.create.content.kinetics.belt.BeltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.function.Supplier;

public class BeltTunnelItem extends BlockItem {

	Supplier<BeltCasingType> casingType;

	public BeltTunnelItem(Block p_i48527_1_, Properties p_i48527_2_, Supplier<BeltCasingType> casingType) {
		super(p_i48527_1_, p_i48527_2_);
		this.casingType = casingType;
	}

	@Override
	protected boolean canPlace(BlockPlaceContext ctx, BlockState state) {
		Player playerentity = ctx.getPlayer();
		CollisionContext iselectioncontext =
			playerentity == null ? CollisionContext.empty() : CollisionContext.of(playerentity);
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		return (!this.mustSurvive() || AllBlocks.ANDESITE_TUNNEL.get()
			.isValidPositionForPlacement(state, world, pos)) && world.isUnobstructed(state, pos, iselectioncontext);
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, Level world, Player p_195943_3_, ItemStack p_195943_4_,
		BlockState state) {
		boolean flag = super.updateCustomBlockEntityTag(pos, world, p_195943_3_, p_195943_4_, state);
		if (!world.isClientSide) {
			BeltBlockEntity belt = BeltHelper.getSegmentBE(world, pos.below());
			if (belt != null && belt.casing == null)
				belt.setCasingType(casingType.get());
		}
		return flag;
	}

}
