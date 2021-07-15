package com.simibubi.create.content.logistics.block.chute;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraft.item.Item.Properties;

public class ChuteItem extends BlockItem {

	public ChuteItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public ActionResultType place(BlockItemUseContext context) {
		Direction face = context.getClickedFace();
		BlockPos placedOnPos = context.getClickedPos()
			.relative(face.getOpposite());
		World world = context.getLevel();
		BlockState placedOnState = world.getBlockState(placedOnPos);

		if (!AbstractChuteBlock.isChute(placedOnState) || context.isSecondaryUseActive())
			return super.place(context);
		if (face.getAxis()
			.isVertical())
			return super.place(context);

		BlockPos correctPos = context.getClickedPos()
			.above();

		BlockState blockState = world.getBlockState(correctPos);
		if (blockState.getMaterial()
			.isReplaceable())
			context = BlockItemUseContext.at(context, correctPos, face);
		else {
			if (!(blockState.getBlock() instanceof ChuteBlock) || world.isClientSide)
				return ActionResultType.FAIL;
			AbstractChuteBlock block = (AbstractChuteBlock) blockState.getBlock();
			if (block.getFacing(blockState) == Direction.DOWN) {
				world.setBlockAndUpdate(correctPos, block.updateChuteState(blockState.setValue(ChuteBlock.FACING, face),
					world.getBlockState(correctPos.above()), world, correctPos));
				return ActionResultType.SUCCESS;
			}
			return ActionResultType.FAIL;
		}

		return super.place(context);
	}

}
