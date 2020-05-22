package com.simibubi.create.modules.contraptions.components.contraptions.chassis;

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
import net.minecraft.world.ILightReader;

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
		if (isChassis(blockState) && !context.getPlayer()
			.isSneaking())
			return getDefaultState().with(AXIS, blockState.get(AXIS));
		if (!context.getPlayer()
			.isSneaking())
			return getDefaultState().with(AXIS, context.getNearestLookingDirection()
				.getAxis());
		return super.getStateForPlacement(context);
	}

	@Override
	public BooleanProperty getGlueableSide(BlockState state, Direction face) {
		if (face.getAxis() != state.get(AXIS))
			return null;
		return face.getAxisDirection() == AxisDirection.POSITIVE ? STICKY_TOP : STICKY_BOTTOM;
	}

	public static boolean isChassis(BlockState state) {
		return AllBlocks.LINEAR_CHASSIS.has(state) || AllBlocks.LINEAR_CHASSIS_SECONDARY.has(state);
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
				return null;
			return state.get(glueableSide) ? AllSpriteShifts.CHASSIS_STICKY : AllSpriteShifts.CHASSIS;
		}

		@Override
		public boolean reverseUVs(BlockState state, Direction face) {
			Axis axis = state.get(AXIS);
			if (axis.isHorizontal() && (axis == Axis.Z ^ face.getAxisDirection() == AxisDirection.POSITIVE))
				return true;
			return super.reverseUVs(state, face);
		}

		@Override
		public boolean connectsTo(BlockState state, BlockState other, ILightReader reader, BlockPos pos,
			BlockPos otherPos, Direction face) {
			return sameKind(state, other) && state.get(AXIS) == other.get(AXIS);
		}

	}

}
