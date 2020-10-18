package com.simibubi.create.content.contraptions.fluids.pipes;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class FluidValveBlock extends DirectionalAxisKineticBlock implements IAxisPipe {

	public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");

	public FluidValveBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(ENABLED, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.FLUID_VALVE.get(getPipeAxis(state));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(ENABLED));
	}

	@Override
	protected boolean prefersConnectionTo(IWorldReader reader, BlockPos pos, Direction facing, boolean shaftAxis) {
		if (!shaftAxis) {
			BlockPos offset = pos.offset(facing);
			BlockState blockState = reader.getBlockState(offset);
			return FluidPipeBlock.canConnectTo(reader, offset, blockState, facing);
		}
		return super.prefersConnectionTo(reader, pos, facing, shaftAxis);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.FLUID_VALVE.create();
	}

	public static Axis getPipeAxis(BlockState state) {
		if (!(state.getBlock() instanceof FluidValveBlock))
			return null;
		Direction facing = state.get(FACING);
		boolean alongFirst = !state.get(AXIS_ALONG_FIRST_COORDINATE);
		for (Axis axis : Iterate.axes) {
			if (axis == facing.getAxis())
				continue;
			if (!alongFirst) {
				alongFirst = true;
				continue;
			}
			return axis;
		}
		return null;
	}

	@Override
	public Axis getAxis(BlockState state) {
		return getPipeAxis(state);
	}

}
