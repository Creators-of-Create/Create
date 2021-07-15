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
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import net.minecraft.block.AbstractBlock.Properties;

public class LinearChassisBlock extends AbstractChassisBlock {

	public static final BooleanProperty STICKY_TOP = BooleanProperty.create("sticky_top");
	public static final BooleanProperty STICKY_BOTTOM = BooleanProperty.create("sticky_bottom");

	public LinearChassisBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(STICKY_TOP, false)
			.setValue(STICKY_BOTTOM, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(STICKY_TOP, STICKY_BOTTOM);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockPos placedOnPos = context.getClickedPos()
			.relative(context.getClickedFace()
				.getOpposite());
		BlockState blockState = context.getLevel()
			.getBlockState(placedOnPos);

		if (context.getPlayer() == null || !context.getPlayer()
			.isShiftKeyDown()) {
			if (isChassis(blockState))
				return defaultBlockState().setValue(AXIS, blockState.getValue(AXIS));
			return defaultBlockState().setValue(AXIS, context.getNearestLookingDirection()
				.getAxis());
		}
		return super.getStateForPlacement(context);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction side, BlockState other, IWorld p_196271_4_,
		BlockPos p_196271_5_, BlockPos p_196271_6_) {
		BooleanProperty property = getGlueableSide(state, side);
		if (property == null || !sameKind(state, other) || state.getValue(AXIS) != other.getValue(AXIS))
			return state;
		return state.setValue(property, false);
	}

	@Override
	public BooleanProperty getGlueableSide(BlockState state, Direction face) {
		if (face.getAxis() != state.getValue(AXIS))
			return null;
		return face.getAxisDirection() == AxisDirection.POSITIVE ? STICKY_TOP : STICKY_BOTTOM;
	}

	@Override
	protected boolean glueAllowedOnSide(IBlockReader world, BlockPos pos, BlockState state, Direction side) {
		BlockState other = world.getBlockState(pos.relative(side));
		return !sameKind(other, state) || state.getValue(AXIS) != other.getValue(AXIS);
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
			return state.getValue(glueableSide) ? AllSpriteShifts.CHASSIS_STICKY : AllSpriteShifts.CHASSIS;
		}

		@Override
		protected Direction getUpDirection(IBlockDisplayReader reader, BlockPos pos, BlockState state, Direction face) {
			Axis axis = state.getValue(AXIS);
			if (face.getAxis() == axis)
				return super.getUpDirection(reader, pos, state, face);
			return Direction.get(AxisDirection.POSITIVE, axis);
		}

		@Override
		protected Direction getRightDirection(IBlockDisplayReader reader, BlockPos pos, BlockState state, Direction face) {
			Axis axis = state.getValue(AXIS);
			return axis != face.getAxis() && axis.isHorizontal() ? (face.getAxis()
				.isHorizontal() ? Direction.DOWN : (axis == Axis.X ? Direction.NORTH : Direction.EAST))
				: super.getRightDirection(reader, pos, state, face);
		}

		@Override
		protected boolean reverseUVsHorizontally(BlockState state, Direction face) {
			Axis axis = state.getValue(AXIS);
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
			Axis axis = state.getValue(AXIS);
			boolean end = face.getAxis() == axis;
			if (end && axis.isHorizontal() && (face.getAxisDirection() == AxisDirection.POSITIVE))
				return true;
			if (!end && axis.isHorizontal() && face == Direction.DOWN)
				return true;
			return super.reverseUVs(state, face);
		}

		@Override
		public boolean connectsTo(BlockState state, BlockState other, IBlockDisplayReader reader, BlockPos pos,
			BlockPos otherPos, Direction face) {
			Axis axis = state.getValue(AXIS);
			boolean superConnect = face.getAxis() == axis ? super.connectsTo(state, other, reader, pos, otherPos, face)
				: sameKind(state, other);
			return superConnect && axis == other.getValue(AXIS);
		}

	}

}
