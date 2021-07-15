package com.simibubi.create.content.logistics.block.depot;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import net.minecraft.item.Item.Properties;

@EventBusSubscriber
public class EjectorItem extends BlockItem {

	public EjectorItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public ActionResultType useOn(ItemUseContext ctx) {
		PlayerEntity player = ctx.getPlayer();
		if (player != null && player.isShiftKeyDown())
			return ActionResultType.SUCCESS;
		return super.useOn(ctx);
	}

	@Override
	protected BlockState getPlacementState(BlockItemUseContext p_195945_1_) {
		BlockState stateForPlacement = super.getPlacementState(p_195945_1_);
		return stateForPlacement;
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, World world, PlayerEntity p_195943_3_, ItemStack p_195943_4_,
		BlockState p_195943_5_) {
		if (world.isClientSide)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> EjectorTargetHandler.flushSettings(pos));
		return super.updateCustomBlockEntityTag(pos, world, p_195943_3_, p_195943_4_, p_195943_5_);
	}

	@Override
	public boolean canAttackBlock(BlockState state, World world, BlockPos pos,
		PlayerEntity p_195938_4_) {
		return !p_195938_4_.isShiftKeyDown();
	}

}
