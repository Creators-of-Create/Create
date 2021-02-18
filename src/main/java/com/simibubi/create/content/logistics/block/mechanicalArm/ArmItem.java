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

@EventBusSubscriber
public class ArmItem extends BlockItem {

	public ArmItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext ctx) {
		World world = ctx.getWorld();
		BlockPos pos = ctx.getPos();
		if (ArmInteractionPoint.isInteractable(world, pos, world.getBlockState(pos)))
			return ActionResultType.SUCCESS;
		return super.onItemUse(ctx);
	}

	@Override
	protected boolean onBlockPlaced(BlockPos pos, World world, PlayerEntity p_195943_3_, ItemStack p_195943_4_,
		BlockState p_195943_5_) {
		if (world.isRemote)
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ArmInteractionPointHandler.flushSettings(pos));
		return super.onBlockPlaced(pos, world, p_195943_3_, p_195943_4_, p_195943_5_);
	}

	@Override
	public boolean canPlayerBreakBlockWhileHolding(BlockState state, World world, BlockPos pos,
		PlayerEntity p_195938_4_) {
		return !ArmInteractionPoint.isInteractable(world, pos, state);
	}

}
