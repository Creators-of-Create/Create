package com.simibubi.create.content.logistics.block.mechanicalArm;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
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
public class ArmItem extends BlockItem {

	public ArmItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public ActionResultType useOn(ItemUseContext ctx) {
		World world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		if (ArmInteractionPoint.isInteractable(world, pos, world.getBlockState(pos)))
			return ActionResultType.SUCCESS;
		return super.useOn(ctx);
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, World world, PlayerEntity pPlayer, ItemStack pStack,
		BlockState pState) {
		if (world.isClientSide)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ArmInteractionPointHandler.flushSettings(pos));
		return super.updateCustomBlockEntityTag(pos, world, pPlayer, pStack, pState);
	}

	@Override
	public boolean canAttackBlock(BlockState state, World world, BlockPos pos,
		PlayerEntity pPlayer) {
		return !ArmInteractionPoint.isInteractable(world, pos, state);
	}

}
