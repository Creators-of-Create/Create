package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.Random;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.fluids.FluidPropagator;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.VoxelShaper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFaceBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.AttachFace;
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

public class SmartFluidPipeBlock extends HorizontalFaceBlock implements IAxisPipe {

	public SmartFluidPipeBlock(Properties p_i48339_1_) {
		super(p_i48339_1_);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(FACE)
			.add(HORIZONTAL_FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx) {
		BlockState stateForPlacement = super.getStateForPlacement(ctx);
		Axis prefferedAxis = null;
		BlockPos pos = ctx.getPos();
		World world = ctx.getWorld();
		for (Direction side : Iterate.directions) {
			if (!prefersConnectionTo(world, pos, side))
				continue;
			if (prefferedAxis != null && prefferedAxis != side.getAxis()) {
				prefferedAxis = null;
				break;
			}
			prefferedAxis = side.getAxis();
		}

		if (prefferedAxis == Axis.Y)
			stateForPlacement = stateForPlacement.with(FACE, AttachFace.WALL)
				.with(HORIZONTAL_FACING, stateForPlacement.get(HORIZONTAL_FACING)
					.getOpposite());
		else if (prefferedAxis != null) {
			if (stateForPlacement.get(FACE) == AttachFace.WALL)
				stateForPlacement = stateForPlacement.with(FACE, AttachFace.FLOOR);
			for (Direction direction : ctx.getNearestLookingDirections()) {
				if (direction.getAxis() != prefferedAxis)
					continue;
				stateForPlacement = stateForPlacement.with(HORIZONTAL_FACING, direction.getOpposite());
			}
		}

		return stateForPlacement;
	}

	protected boolean prefersConnectionTo(IWorldReader reader, BlockPos pos, Direction facing) {
		BlockPos offset = pos.offset(facing);
		BlockState blockState = reader.getBlockState(offset);
		return FluidPipeBlock.canConnectTo(reader, offset, blockState, facing);
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

	protected static Axis getPipeAxis(BlockState state) {
		return state.get(FACE) == AttachFace.WALL ? Axis.Y
			: state.get(HORIZONTAL_FACING)
				.getAxis();
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return AllTileEntities.SMART_FLUID_PIPE.create();
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random r) {
		FluidPropagator.propagateChangedPipe(world, pos, state);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		AttachFace face = state.get(FACE);
		VoxelShaper shape = face == AttachFace.FLOOR ? AllShapes.SMART_FLUID_PIPE_FLOOR
			: face == AttachFace.CEILING ? AllShapes.SMART_FLUID_PIPE_CEILING : AllShapes.SMART_FLUID_PIPE_WALL;
		return shape.get(state.get(HORIZONTAL_FACING));
	}

	@Override
	public Axis getAxis(BlockState state) {
		return getPipeAxis(state);
	}

}
