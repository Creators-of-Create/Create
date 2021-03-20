package com.simibubi.create.content.contraptions.fluids.pipes;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.fluids.FluidPropagator;
import com.simibubi.create.foundation.utility.Iterate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.DebugPacketSender;
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
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import java.util.Random;

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

	@Nonnull
	public static Axis getPipeAxis(BlockState state) {
		if (!(state.getBlock() instanceof FluidValveBlock))
			throw new IllegalStateException("Provided BlockState is for a different block.");
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
		throw new IllegalStateException("Impossible axis.");
	}

	@Override
	public Axis getAxis(BlockState state) {
		return getPipeAxis(state);
	}

	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		boolean blockTypeChanged = state.getBlock() != newState.getBlock();
		if (blockTypeChanged && !world.isRemote)
			FluidPropagator.propagateChangedPipe(world, pos, state);
		if (state.hasTileEntity() && (blockTypeChanged || !newState.hasTileEntity()))
			world.removeTileEntity(pos);
	}

	@Override
	public boolean isValidPosition(BlockState p_196260_1_, IWorldReader p_196260_2_, BlockPos p_196260_3_) {
		return true;
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (world.isRemote)
			return;
		if (state != oldState)
			world.getPendingBlockTicks()
				.scheduleTick(pos, this, 1, TickPriority.HIGH);
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block otherBlock, BlockPos neighborPos,
		boolean isMoving) {
		DebugPacketSender.func_218806_a(world, pos);
		Direction d = FluidPropagator.validateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
		if (d == null)
			return;
		if (!isOpenAt(state, d))
			return;
		world.getPendingBlockTicks()
			.scheduleTick(pos, this, 1, TickPriority.HIGH);
	}

	public static boolean isOpenAt(BlockState state, Direction d) {
		return d.getAxis() == getPipeAxis(state);
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random r) {
		FluidPropagator.propagateChangedPipe(world, pos, state);
	}

}
