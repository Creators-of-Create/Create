package com.simibubi.create.content.contraptions.fluids.actors;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.HorizontalKineticBlock;
import com.simibubi.create.content.contraptions.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.foundation.block.ITE;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class HosePulleyBlock extends HorizontalKineticBlock implements ITE<HosePulleyTileEntity> {

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
	public void onRemove(BlockState p_196243_1_, Level world, BlockPos pos, BlockState p_196243_4_,
		boolean p_196243_5_) {
		if (p_196243_1_.hasBlockEntity()
			&& (p_196243_1_.getBlock() != p_196243_4_.getBlock() || !p_196243_4_.hasBlockEntity())) {
			TileEntityBehaviour.destroy(world, pos, FluidDrainingBehaviour.TYPE);
			TileEntityBehaviour.destroy(world, pos, FluidFillingBehaviour.TYPE);
			world.removeBlockEntity(pos);
		}
	}

	@Override
	public Class<HosePulleyTileEntity> getTileEntityClass() {
		return HosePulleyTileEntity.class;
	}
	
	@Override
	public BlockEntityType<? extends HosePulleyTileEntity> getTileEntityType() {
		return AllTileEntities.HOSE_PULLEY.get();
	}

}
