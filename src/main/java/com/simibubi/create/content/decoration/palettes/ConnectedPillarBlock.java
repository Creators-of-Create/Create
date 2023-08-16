package com.simibubi.create.content.decoration.palettes;

import net.createmod.catnip.utility.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.ticks.LevelTickAccess;

public class ConnectedPillarBlock extends LayeredBlock {

	public static final BooleanProperty NORTH = BooleanProperty.create("north");
	public static final BooleanProperty SOUTH = BooleanProperty.create("south");
	public static final BooleanProperty EAST = BooleanProperty.create("east");
	public static final BooleanProperty WEST = BooleanProperty.create("west");

	public ConnectedPillarBlock(Properties p_55926_) {
		super(p_55926_);
		registerDefaultState(defaultBlockState().setValue(NORTH, false)
			.setValue(WEST, false)
			.setValue(EAST, false)
			.setValue(SOUTH, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder.add(NORTH, SOUTH, EAST, WEST));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		BlockState state = super.getStateForPlacement(pContext);
		return updateColumn(pContext.getLevel(), pContext.getClickedPos(), state, true);
	}

	private BlockState updateColumn(Level level, BlockPos pos, BlockState state, boolean present) {
		MutableBlockPos currentPos = new MutableBlockPos();
		Axis axis = state.getValue(AXIS);

		for (Direction connection : Iterate.directions) {
			if (connection.getAxis() == axis)
				continue;

			boolean connect = true;
			Move: for (Direction movement : Iterate.directionsInAxis(axis)) {
				currentPos.set(pos);
				for (int i = 0; i < 1000; i++) {
					if (!level.isLoaded(currentPos))
						break;

					BlockState other1 = currentPos.equals(pos) ? state : level.getBlockState(currentPos);
					BlockState other2 = level.getBlockState(currentPos.relative(connection));
					boolean col1 = canConnect(state, other1);
					boolean col2 = canConnect(state, other2);
					currentPos.move(movement);

					if (!col1 && !col2)
						break;
					if (col1 && col2)
						continue;

					connect = false;
					break Move;
				}
			}
			state = setConnection(state, connection, connect);
		}
		return state;
	}

	@Override
	public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
		if (pOldState.getBlock() == this)
			return;
		LevelTickAccess<Block> blockTicks = pLevel.getBlockTicks();
		if (!blockTicks.hasScheduledTick(pPos, this))
			pLevel.scheduleTick(pPos, this, 1);
	}

	@Override
	public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
		if (pState.getBlock() != this)
			return;
		BlockPos belowPos =
			pPos.relative(Direction.fromAxisAndDirection(pState.getValue(AXIS), AxisDirection.NEGATIVE));
		BlockState belowState = pLevel.getBlockState(belowPos);
		if (!canConnect(pState, belowState))
			pLevel.setBlock(pPos, updateColumn(pLevel, pPos, pState, true), 3);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction pDirection, BlockState pNeighborState,
		LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
		if (!canConnect(state, pNeighborState))
			return setConnection(state, pDirection, false);
		if (pDirection.getAxis() == state.getValue(AXIS))
			return withPropertiesOf(pNeighborState);

		return setConnection(state, pDirection, getConnection(pNeighborState, pDirection.getOpposite()));
	}

	protected boolean canConnect(BlockState state, BlockState other) {
		return other.getBlock() == this && state.getValue(AXIS) == other.getValue(AXIS);
	}

	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		if (pIsMoving || pNewState.getBlock() == this)
			return;
		for (Direction d : Iterate.directionsInAxis(pState.getValue(AXIS))) {
			BlockPos relative = pPos.relative(d);
			BlockState adjacent = pLevel.getBlockState(relative);
			if (canConnect(pState, adjacent))
				pLevel.setBlock(relative, updateColumn(pLevel, relative, adjacent, false), 3);
		}
	}

	public static boolean getConnection(BlockState state, Direction side) {
		BooleanProperty property = connection(state.getValue(AXIS), side);
		return property != null && state.getValue(property);
	}

	public static BlockState setConnection(BlockState state, Direction side, boolean connect) {
		BooleanProperty property = connection(state.getValue(AXIS), side);
		if (property != null)
			state = state.setValue(property, connect);
		return state;
	}

	public static BooleanProperty connection(Axis axis, Direction side) {
		if (side.getAxis() == axis)
			return null;

		if (axis == Axis.X) {
			switch (side) {
			case UP:
				return EAST;
			case NORTH:
				return NORTH;
			case SOUTH:
				return SOUTH;
			case DOWN:
				return WEST;
			default:
				return null;
			}
		}

		if (axis == Axis.Y) {
			switch (side) {
			case EAST:
				return EAST;
			case NORTH:
				return NORTH;
			case SOUTH:
				return SOUTH;
			case WEST:
				return WEST;
			default:
				return null;
			}
		}

		if (axis == Axis.Z) {
			switch (side) {
			case UP:
				return WEST;
			case WEST:
				return SOUTH;
			case EAST:
				return NORTH;
			case DOWN:
				return EAST;
			default:
				return null;
			}
		}

		return null;
	}

}
