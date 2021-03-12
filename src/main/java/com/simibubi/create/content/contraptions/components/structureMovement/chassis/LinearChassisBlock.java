package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ILightReader;
import net.minecraft.world.IWorld;

public class LinearChassisBlock extends AbstractChassisBlock {

	public static final BooleanProperty STICKY_TOP = BooleanProperty.create("sticky_top");
	public static final BooleanProperty STICKY_BOTTOM = BooleanProperty.create("sticky_bottom");

	public LinearChassisBlock(Properties properties) {
		super(properties);
		setDefaultState(getDefaultState().with(STICKY_TOP, false)
			.with(STICKY_BOTTOM, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(STICKY_TOP, STICKY_BOTTOM);
		super.fillStateContainer(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockPos placedOnPos = context.getPos()
			.offset(context.getFace()
				.getOpposite());
		BlockState blockState = context.getWorld()
			.getBlockState(placedOnPos);

		if (context.getPlayer() == null || !context.getPlayer()
			.isSneaking()) {
			if (isChassis(blockState))
				return getDefaultState().with(AXIS, blockState.get(AXIS));
			return getDefaultState().with(AXIS, context.getNearestLookingDirection()
				.getAxis());
		}
		return super.getStateForPlacement(context);
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState state, Direction side, BlockState other, IWorld p_196271_4_,
		BlockPos p_196271_5_, BlockPos p_196271_6_) {
		BooleanProperty property = getGlueableSide(state, side);
		if (property == null || !sameKind(state, other) || state.get(AXIS) != other.get(AXIS))
			return state;
		return state.with(property, false);
	}

	@Override
	public BooleanProperty getGlueableSide(BlockState state, Direction face) {
		if (face.getAxis() != state.get(AXIS))
			return null;
		return face.getAxisDirection() == AxisDirection.POSITIVE ? STICKY_TOP : STICKY_BOTTOM;
	}

	@Override
	protected boolean glueAllowedOnSide(IBlockReader world, BlockPos pos, BlockState state, Direction side) {
		BlockState other = world.getBlockState(pos.offset(side));
		return !sameKind(other, state) || state.get(AXIS) != other.get(AXIS);
	}

	public static boolean isChassis(BlockState state) {
		return AllBlocks.LINEAR_CHASSIS.has(state) || AllBlocks.SECONDARY_LINEAR_CHASSIS.has(state);
	}

	public static boolean sameKind(BlockState state1, BlockState state2) {
		return state1.getBlock() == state2.getBlock();
	}

	public static class ChassisCTBehaviour extends ConnectedTextureBehaviour {

		@Override
		public CTSpriteShiftEntry get(BlockState state, Direction direction) {
			Block block = state.getBlock();
			BooleanProperty glueableSide = ((LinearChassisBlock) block).getGlueableSide(state, direction);
			if (glueableSide == null)
				return AllBlocks.LINEAR_CHASSIS.has(state) ? AllSpriteShifts.CHASSIS_SIDE
					: AllSpriteShifts.SECONDARY_CHASSIS_SIDE;
			return state.get(glueableSide) ? AllSpriteShifts.CHASSIS_STICKY : AllSpriteShifts.CHASSIS;
		}

		@Override
		protected Direction getUpDirection(ILightReader reader, BlockPos pos, BlockState state, Direction face) {
			Axis axis = state.get(AXIS);
			if (face.getAxis() == axis)
				return super.getUpDirection(reader, pos, state, face);
			return Direction.getFacingFromAxis(AxisDirection.POSITIVE, axis);
		}

		@Override
		protected Direction getRightDirection(ILightReader reader, BlockPos pos, BlockState state, Direction face) {
			Axis axis = state.get(AXIS);
			return axis != face.getAxis() && axis.isHorizontal() ? (face.getAxis()
				.isHorizontal() ? Direction.DOWN : (axis == Axis.X ? Direction.NORTH : Direction.EAST))
				: super.getRightDirection(reader, pos, state, face);
		}

		@Override
		protected boolean reverseUVsHorizontally(BlockState state, Direction face) {
			Axis axis = state.get(AXIS);
			boolean side = face.getAxis() != axis;
			if (side && axis == Axis.X && face.getAxis()
				.isHorizontal())
				return true;
			return super.reverseUVsHorizontally(state, face);
		}

		@Override
		protected boolean reverseUVsVertically(BlockState state, Direction face) {
			return super.reverseUVsVertically(state, face);
		}

		@Override
		public boolean reverseUVs(BlockState state, Direction face) {
			Axis axis = state.get(AXIS);
			boolean end = face.getAxis() == axis;
			if (end && axis.isHorizontal() && (face.getAxisDirection() == AxisDirection.POSITIVE))
				return true;
			if (!end && axis.isHorizontal() && face == Direction.DOWN)
				return true;
			return super.reverseUVs(state, face);
		}

		@Override
		public boolean connectsTo(BlockState state, BlockState other, ILightReader reader, BlockPos pos,
			BlockPos otherPos, Direction face) {
			Axis axis = state.get(AXIS);
			boolean superConnect = face.getAxis() == axis ? super.connectsTo(state, other, reader, pos, otherPos, face)
				: sameKind(state, other);
			return superConnect && axis == other.get(AXIS);
		}

	}

}
