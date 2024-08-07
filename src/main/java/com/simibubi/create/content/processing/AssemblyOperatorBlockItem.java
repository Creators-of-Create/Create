package com.simibubi.create.content.processing;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.belt.BeltBlock;
import com.simibubi.create.content.kinetics.belt.BeltSlope;
import com.simibubi.create.content.processing.basin.BasinBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
		Level level = context.getLevel();
		BlockState placedOnState = level
			.getBlockState(placedOnPos);
		if (operatesOn(level, placedOnPos, placedOnState) && context.getClickedFace() == Direction.UP) {
			if (level.getBlockState(placedOnPos.above(2))
				.canBeReplaced())
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

	protected boolean operatesOn(LevelReader world, BlockPos pos, BlockState placedOnState) {
		if (AllBlocks.BELT.has(placedOnState))
			return placedOnState.getValue(BeltBlock.SLOPE) == BeltSlope.HORIZONTAL;
		return BasinBlock.isBasin(world, pos) || AllBlocks.DEPOT.has(placedOnState) || AllBlocks.WEIGHTED_EJECTOR.has(placedOnState);
	}

}
