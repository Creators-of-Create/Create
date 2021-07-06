package com.simibubi.create.content.contraptions.components;

import com.simibubi.create.AllBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

public class AssemblyOperatorBlockItem extends BlockItem {

	public AssemblyOperatorBlockItem(Block block, Properties builder) {
		super(block, builder);
	}

	@Override
	public ActionResultType tryPlace(BlockItemUseContext context) {
		BlockPos placedOnPos = context.getPos()
			.offset(context.getFace()
				.getOpposite());
		BlockState placedOnState = context.getWorld()
			.getBlockState(placedOnPos);
		if (operatesOn(placedOnState)) {
			if (context.getWorld()
				.getBlockState(placedOnPos.up(2))
				.getMaterial()
				.isReplaceable())
				context = adjustContext(context, placedOnPos);
			else
				return ActionResultType.FAIL;
		}

		return super.tryPlace(context);
	}

	protected BlockItemUseContext adjustContext(BlockItemUseContext context, BlockPos placedOnPos) {
		BlockPos up = placedOnPos.up(2);
		return new AssemblyOperatorUseContext(context.getWorld(), context.getPlayer(), context.getHand(), context.getItem(), new BlockRayTraceResult(new Vector3d((double)up.getX() + 0.5D + (double) Direction.UP.getXOffset() * 0.5D, (double)up.getY() + 0.5D + (double) Direction.UP.getYOffset() * 0.5D, (double)up.getZ() + 0.5D + (double) Direction.UP.getZOffset() * 0.5D), Direction.UP, up, false));
	}

	protected boolean operatesOn(BlockState placedOnState) {
		return AllBlocks.BASIN.has(placedOnState) || AllBlocks.BELT.has(placedOnState) || AllBlocks.DEPOT.has(placedOnState) || AllBlocks.WEIGHTED_EJECTOR.has(placedOnState);
	}

}
