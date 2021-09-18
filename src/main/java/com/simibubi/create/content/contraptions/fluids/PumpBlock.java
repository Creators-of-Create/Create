package com.simibubi.create.content.contraptions.fluids;

import java.util.Random;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllShapes;
import com.simibubi.create.AllTileEntities;
import com.simibubi.create.content.contraptions.base.DirectionalKineticBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.TickPriority;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class PumpBlock extends DirectionalKineticBlock implements SimpleWaterloggedBlock, ICogWheel {

	public PumpBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
		registerDefaultState(super.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return AllTileEntities.MECHANICAL_PUMP.create(pos, state);
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		return originalState.setValue(FACING, originalState.getValue(FACING)
			.getOpposite());
	}

	@Override
	public BlockState updateAfterWrenched(BlockState newState, UseOnContext context) {
		BlockState state = super.updateAfterWrenched(newState, context);
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (world.isClientSide)
			return state;
		BlockEntity tileEntity = world.getBlockEntity(pos);
		if (!(tileEntity instanceof PumpTileEntity))
			return state;
		PumpTileEntity pump = (PumpTileEntity) tileEntity;
		pump.sidesToUpdate.forEach(MutableBoolean::setTrue);
		pump.reversed = !pump.reversed;
		return state;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING)
			.getAxis();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_,
		CollisionContext p_220053_4_) {
		return AllShapes.PUMP.get(state.getValue(FACING));
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block otherBlock, BlockPos neighborPos,
		boolean isMoving) {
		DebugPackets.sendNeighborsUpdatePacket(world, pos);
		Direction d = FluidPropagator.validateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
		if (d == null)
			return;
		if (!isOpenAt(state, d))
			return;
		world.getBlockTicks()
			.scheduleTick(pos, this, 1, TickPriority.HIGH);
//		if (world.isRemote)
//			return;
//		if (otherBlock instanceof FluidPipeBlock)
//			return;
//		TileEntity tileEntity = world.getTileEntity(pos);
//		if (!(tileEntity instanceof PumpTileEntity))
//			return;
//		PumpTileEntity pump = (PumpTileEntity) tileEntity;
//		Direction facing = state.get(FACING);
//		for (boolean front : Iterate.trueAndFalse) {
//			Direction side = front ? facing : facing.getOpposite();
//			if (!pos.offset(side)
//				.equals(neighborPos))
//				continue;
//			pump.updatePipesOnSide(side);
//		}
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false)
			: Fluids.EMPTY.defaultFluidState();
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.WATERLOGGED);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
		LevelAccessor world, BlockPos pos, BlockPos neighbourPos) {
		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			world.getLiquidTicks()
				.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}
		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState FluidState = context.getLevel()
			.getFluidState(context.getClickedPos());
		return super.getStateForPlacement(context).setValue(BlockStateProperties.WATERLOGGED,
			Boolean.valueOf(FluidState.getType() == Fluids.WATER));
	}

	public static boolean isPump(BlockState state) {
		return state.getBlock() instanceof PumpBlock;
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (world.isClientSide)
			return;
		if (state != oldState)
			world.getBlockTicks()
				.scheduleTick(pos, this, 1, TickPriority.HIGH);
	}

	public static boolean isOpenAt(BlockState state, Direction d) {
		return d.getAxis() == state.getValue(FACING)
			.getAxis();
	}

	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, Random r) {
		FluidPropagator.propagateChangedPipe(world, pos, state);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		boolean blockTypeChanged = state.getBlock() != newState.getBlock();
		if (blockTypeChanged && !world.isClientSide)
			FluidPropagator.propagateChangedPipe(world, pos, state);
		if (blockTypeChanged)
			world.removeBlockEntity(pos);
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

}
