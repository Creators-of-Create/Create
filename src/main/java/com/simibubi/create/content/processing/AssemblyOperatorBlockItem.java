package com.simibubi.create.content.processing;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltSlope;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AssemblyOperatorBlockItem extends BlockItem {

	public AssemblyOperatorBlockItem(Block block, Properties builder) {
		super(block, builder);
	}

	@Override
	public InteractionResult place(BlockPlaceContext context) {
		BlockPos placedOnPos = context.getClickedPos()
			.relative(context.getClickedFace()
				.getOpposite());
		BlockState placedOnState = context.getLevel()
			.getBlockState(placedOnPos);
		if (operatesOn(placedOnState) && context.getClickedFace() == Direction.UP) {
			if (context.getLevel()
				.getBlockState(placedOnPos.above(2))
				.getMaterial()
				.isReplaceable())
				context = adjustContext(context, placedOnPos);
			else
				return InteractionResult.FAIL;
		}

		return super.place(context);
	}

	protected BlockPlaceContext adjustContext(BlockPlaceContext context, BlockPos placedOnPos) {
		BlockPos up = placedOnPos.above(2);
		return new AssemblyOperatorUseContext(context.getLevel(), context.getPlayer(), context.getHand(), context.getItemInHand(), new BlockHitResult(new Vec3((double)up.getX() + 0.5D + (double) Direction.UP.getStepX() * 0.5D, (double)up.getY() + 0.5D + (double) Direction.UP.getStepY() * 0.5D, (double)up.getZ() + 0.5D + (double) Direction.UP.getStepZ() * 0.5D), Direction.UP, up, false));
	}

	protected boolean operatesOn(BlockState placedOnState) {
		if (AllBlocks.BELT.has(placedOnState))
			return placedOnState.getValue(BeltBlock.SLOPE) == BeltSlope.HORIZONTAL;
		return AllBlocks.BASIN.has(placedOnState) || AllBlocks.DEPOT.has(placedOnState) || AllBlocks.WEIGHTED_EJECTOR.has(placedOnState);
	}

}
