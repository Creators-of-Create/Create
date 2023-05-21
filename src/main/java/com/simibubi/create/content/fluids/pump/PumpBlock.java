package com.simibubi.create.content.fluids.pump;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;

public class PumpBlock extends DirectionalKineticBlock
	implements SimpleWaterloggedBlock, ICogWheel, IBE<PumpBlockEntity> {

	public PumpBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
		registerDefaultState(super.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		return originalState.setValue(FACING, originalState.getValue(FACING)
			.getOpposite());
	}

	@Override
	public BlockState updateAfterWrenched(BlockState newState, UseOnContext context) {
		return super.updateAfterWrenched(newState, context);
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
		world.scheduleTick(pos, this, 1, TickPriority.HIGH);
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
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world,
		BlockPos pos, BlockPos neighbourPos) {
		if (state.getValue(BlockStateProperties.WATERLOGGED))
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState toPlace = super.getStateForPlacement(context);
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		toPlace = ProperWaterloggedBlock.withWater(level, toPlace, pos);

		Direction nearestLookingDirection = context.getNearestLookingDirection();
		Direction targetDirection = context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown() ? nearestLookingDirection : nearestLookingDirection.getOpposite();
		Direction bestConnectedDirection = null;
		double bestDistance = Double.MAX_VALUE;

		for (Direction d : Iterate.directions) {
			BlockPos adjPos = pos.relative(d);
			BlockState adjState = level.getBlockState(adjPos);
			if (!FluidPipeBlock.canConnectTo(level, adjPos, adjState, d))
				continue;
			double distance = Vec3.atLowerCornerOf(d.getNormal())
				.distanceTo(Vec3.atLowerCornerOf(targetDirection.getNormal()));
			if (distance > bestDistance)
				continue;
			bestDistance = distance;
			bestConnectedDirection = d;
		}

		if (bestConnectedDirection == null)
			return toPlace;
		if (bestConnectedDirection.getAxis() == targetDirection.getAxis())
			return toPlace;
		if (player.isSteppingCarefully() && bestConnectedDirection.getAxis() != targetDirection.getAxis())
			return toPlace;

		return toPlace.setValue(FACING, bestConnectedDirection);
	}

	public static boolean isPump(BlockState state) {
		return state.getBlock() instanceof PumpBlock;
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onPlace(state, world, pos, oldState, isMoving);
		if (world.isClientSide)
			return;
		if (state != oldState)
			world.scheduleTick(pos, this, 1, TickPriority.HIGH);

		if (isPump(state) && isPump(oldState) && state.getValue(FACING) == oldState.getValue(FACING)
			.getOpposite()) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (!(blockEntity instanceof PumpBlockEntity))
				return;
			PumpBlockEntity pump = (PumpBlockEntity) blockEntity;
			pump.pressureUpdate = true;
		}
	}

	public static boolean isOpenAt(BlockState state, Direction d) {
		return d.getAxis() == state.getValue(FACING)
			.getAxis();
	}

	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource r) {
		FluidPropagator.propagateChangedPipe(world, pos, state);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		boolean blockTypeChanged = !state.is(newState.getBlock());
		if (blockTypeChanged && !world.isClientSide)
			FluidPropagator.propagateChangedPipe(world, pos, state);
		super.onRemove(state, world, pos, newState, isMoving);
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

	@Override
	public Class<PumpBlockEntity> getBlockEntityClass() {
		return PumpBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends PumpBlockEntity> getBlockEntityType() {
		return AllBlockEntityTypes.MECHANICAL_PUMP.get();
	}

}
