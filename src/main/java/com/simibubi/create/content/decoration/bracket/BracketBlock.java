package com.simibubi.create.content.decoration.bracket;

import java.util.Optional;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class BracketBlock extends WrenchableDirectionalBlock {

	public static final BooleanProperty AXIS_ALONG_FIRST_COORDINATE =
		DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
	public static final EnumProperty<BracketType> TYPE = EnumProperty.create("type", BracketType.class);

	public static enum BracketType implements StringRepresentable {
		PIPE, COG, SHAFT;

		@Override
		public String getSerializedName() {
			return Lang.asId(name());
		}

	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(AXIS_ALONG_FIRST_COORDINATE)
			.add(TYPE));
	}

	public BracketBlock(Properties properties) {
		super(properties);
	}

	public Optional<BlockState> getSuitableBracket(BlockState blockState, Direction direction) {
		if (blockState.getBlock() instanceof AbstractSimpleShaftBlock)
			return getSuitableBracket(blockState.getValue(RotatedPillarKineticBlock.AXIS), direction,
				blockState.getBlock() instanceof CogWheelBlock ? BracketType.COG : BracketType.SHAFT);
		return getSuitableBracket(FluidPropagator.getStraightPipeAxis(blockState), direction, BracketType.PIPE);
	}

	private Optional<BlockState> getSuitableBracket(Axis targetBlockAxis, Direction direction, BracketType type) {
		Axis axis = direction.getAxis();
		if (targetBlockAxis == null || targetBlockAxis == axis)
			return Optional.empty();

		boolean alongFirst = axis != Axis.Z ? targetBlockAxis == Axis.Z : targetBlockAxis == Axis.Y;
		return Optional.of(defaultBlockState().setValue(TYPE, type)
			.setValue(FACING, direction)
			.setValue(AXIS_ALONG_FIRST_COORDINATE, !alongFirst));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		if (rot.ordinal() % 2 == 1)
			state = state.cycle(AXIS_ALONG_FIRST_COORDINATE);
		return super.rotate(state, rot);
	}

}
