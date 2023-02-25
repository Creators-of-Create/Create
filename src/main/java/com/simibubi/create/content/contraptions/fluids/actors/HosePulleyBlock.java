package com.simibubi.create.content.contraptions.fluids.actors;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class HosePulleyBlock extends HorizontalKineticBlock implements IBE<HosePulleyBlockEntity> {

	public HosePulleyBlock(Properties properties) {
		super(properties);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING)
			.getClockWise()
			.getAxis();
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction preferredHorizontalFacing = getPreferredHorizontalFacing(context);
		return this.defaultBlockState()
			.setValue(HORIZONTAL_FACING,
				preferredHorizontalFacing != null ? preferredHorizontalFacing.getCounterClockWise()
					: context.getHorizontalDirection()
						.getOpposite());
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return state.getValue(HORIZONTAL_FACING)
			.getClockWise() == face;
	}

	public static boolean hasPipeTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return state.getValue(HORIZONTAL_FACING)
			.getCounterClockWise() == face;
	}

	@Override
	public Direction getPreferredHorizontalFacing(BlockPlaceContext context) {
		Direction fromParent = super.getPreferredHorizontalFacing(context);
		if (fromParent != null)
			return fromParent;

		Direction prefferedSide = null;
		for (Direction facing : Iterate.horizontalDirections) {
			BlockPos pos = context.getClickedPos()
				.relative(facing);
			BlockState blockState = context.getLevel()
				.getBlockState(pos);
			if (FluidPipeBlock.canConnectTo(context.getLevel(), pos, blockState, facing))
				if (prefferedSide != null && prefferedSide.getAxis() != facing.getAxis()) {
					prefferedSide = null;
					break;
				} else
					prefferedSide = facing;
		}
		return prefferedSide == null ? null : prefferedSide.getOpposite();
	}
	
	@Override
	public Class<HosePulleyBlockEntity> getBlockEntityClass() {
		return HosePulleyBlockEntity.class;
	}
	
	@Override
	public BlockEntityType<? extends HosePulleyBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.HOSE_PULLEY.get();
	}

}
