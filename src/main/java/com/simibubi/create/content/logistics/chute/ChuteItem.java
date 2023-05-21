package com.simibubi.create.content.logistics.chute;

import com.simibubi.create.foundation.block.ProperWaterloggedBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ChuteItem extends BlockItem {

	public ChuteItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public InteractionResult place(BlockPlaceContext context) {
		Direction face = context.getClickedFace();
		BlockPos placedOnPos = context.getClickedPos()
			.relative(face.getOpposite());
		Level world = context.getLevel();
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
			context = BlockPlaceContext.at(context, correctPos, face);
		else {
			if (!(blockState.getBlock() instanceof ChuteBlock) || world.isClientSide)
				return InteractionResult.FAIL;
			AbstractChuteBlock block = (AbstractChuteBlock) blockState.getBlock();
			if (block.getFacing(blockState) == Direction.DOWN) {
				world.setBlockAndUpdate(correctPos,
					ProperWaterloggedBlock.withWater(world,
						block.updateChuteState(blockState.setValue(ChuteBlock.FACING, face),
							world.getBlockState(correctPos.above()), world, correctPos),
						correctPos));
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.FAIL;
		}

		return super.place(context);
	}

}
