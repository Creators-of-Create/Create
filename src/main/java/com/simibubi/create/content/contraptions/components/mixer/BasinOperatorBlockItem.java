package com.simibubi.create.content.contraptions.components.mixer;

import com.simibubi.create.AllBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class BasinOperatorBlockItem extends BlockItem {

	public BasinOperatorBlockItem(Block block, Properties builder) {
		super(block, builder);
	}

	@Override
	public ActionResultType tryPlace(BlockItemUseContext context) {
		BlockPos placedOnPos = context.getPos()
			.offset(context.getFace()
				.getOpposite());
		BlockState placedOnState = context.getWorld()
			.getBlockState(placedOnPos);
		if (AllBlocks.BASIN.has(placedOnState) || AllBlocks.BELT.has(placedOnState)
			|| AllBlocks.DEPOT.has(placedOnState)) {
			if (context.getWorld()
				.getBlockState(placedOnPos.up(2))
				.getMaterial()
				.isReplaceable())
				context = BlockItemUseContext.func_221536_a(context, placedOnPos.up(2), Direction.UP);
			else
				return ActionResultType.FAIL;
		}

		return super.tryPlace(context);
	}

}