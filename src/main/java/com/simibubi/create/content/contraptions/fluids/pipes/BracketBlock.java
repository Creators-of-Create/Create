package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.Optional;

import com.simibubi.create.content.contraptions.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.contraptions.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.contraptions.fluids.FluidPropagator;
import com.simibubi.create.content.contraptions.relays.elementary.CogWheelBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.IStringSerializable;

public class BracketBlock extends ProperDirectionalBlock {

	public static final BooleanProperty AXIS_ALONG_FIRST_COORDINATE =
		DirectionalAxisKineticBlock.AXIS_ALONG_FIRST_COORDINATE;
	public static final EnumProperty<BracketType> TYPE = EnumProperty.create("type", BracketType.class);

	public static enum BracketType implements IStringSerializable {
		PIPE, COG, SHAFT;

		@Override
		public String getString() {
			return Lang.asId(name());
		}

	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder.add(AXIS_ALONG_FIRST_COORDINATE)
			.add(TYPE));
	}

	public BracketBlock(Properties p_i48415_1_) {
		super(p_i48415_1_);
	}

	public Optional<BlockState> getSuitableBracket(BlockState blockState, Direction direction) {
		if (blockState.getBlock() instanceof ShaftBlock)
			return getSuitableBracket(blockState.get(RotatedPillarKineticBlock.AXIS), direction,
				blockState.getBlock() instanceof CogWheelBlock ? BracketType.COG : BracketType.SHAFT);
		return getSuitableBracket(FluidPropagator.getStraightPipeAxis(blockState), direction, BracketType.PIPE);
	}

	private Optional<BlockState> getSuitableBracket(Axis targetBlockAxis, Direction direction, BracketType type) {
		Axis axis = direction.getAxis();
		if (targetBlockAxis == null || targetBlockAxis == axis)
			return Optional.empty();

		boolean alongFirst = axis != Axis.Z ? targetBlockAxis == Axis.Z : targetBlockAxis == Axis.Y;
		return Optional.of(getDefaultState().with(TYPE, type)
			.with(FACING, direction)
			.with(AXIS_ALONG_FIRST_COORDINATE, !alongFirst));
	}

}
