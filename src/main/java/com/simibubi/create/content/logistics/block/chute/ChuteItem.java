package com.simibubi.create.content.logistics.block.chute;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChuteItem extends BlockItem {

	public ChuteItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public ActionResultType tryPlace(BlockItemUseContext context) {
		Direction face = context.getFace();
		BlockPos placedOnPos = context.getPos()
			.offset(face.getOpposite());
		World world = context.getWorld();
		BlockState placedOnState = world.getBlockState(placedOnPos);

		if (!AbstractChuteBlock.isChute(placedOnState) || context.shouldCancelInteraction())
			return super.tryPlace(context);
		if (face.getAxis()
			.isVertical())
			return super.tryPlace(context);

		BlockPos correctPos = context.getPos()
			.up();

		BlockState blockState = world.getBlockState(correctPos);
		if (blockState.getMaterial()
			.isReplaceable())
			context = BlockItemUseContext.func_221536_a(context, correctPos, face);
		else {
			if (!(blockState.getBlock() instanceof ChuteBlock) || world.isRemote)
				return ActionResultType.FAIL;
			AbstractChuteBlock block = (AbstractChuteBlock) blockState.getBlock();
			if (block.getFacing(blockState) == Direction.DOWN) {
				world.setBlockState(correctPos, block.updateChuteState(blockState.with(ChuteBlock.FACING, face),
					world.getBlockState(correctPos.up()), world, correctPos));
				return ActionResultType.SUCCESS;
			}
			return ActionResultType.FAIL;
		}

		return super.tryPlace(context);
	}

}
