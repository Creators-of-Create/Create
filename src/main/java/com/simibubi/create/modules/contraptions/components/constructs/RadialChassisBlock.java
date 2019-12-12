package com.simibubi.create.modules.contraptions.components.constructs;

import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class RadialChassisBlock extends AbstractChassisBlock {

	public static final BooleanProperty STICKY_NORTH = BooleanProperty.create("sticky_north");
	public static final BooleanProperty STICKY_SOUTH = BooleanProperty.create("sticky_south");
	public static final BooleanProperty STICKY_EAST = BooleanProperty.create("sticky_east");
	public static final BooleanProperty STICKY_WEST = BooleanProperty.create("sticky_west");

	public RadialChassisBlock() {
		super(Properties.from(Blocks.PISTON));
		setDefaultState(getDefaultState().with(STICKY_EAST, false).with(STICKY_SOUTH, false).with(STICKY_NORTH, false)
				.with(STICKY_WEST, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(STICKY_NORTH, STICKY_EAST, STICKY_SOUTH, STICKY_WEST);
		super.fillStateContainer(builder);
	}

	@Override
	public String getValueName(BlockState state, IWorld world, BlockPos pos) {
		return Lang.translate("generic.radius");
	}

	@Override
	public BooleanProperty getGlueableSide(BlockState state, Direction face) {
		Axis axis = state.get(AXIS);

		if (axis == Axis.X) {
			if (face == Direction.NORTH)
				return STICKY_WEST;
			if (face == Direction.SOUTH)
				return STICKY_EAST;
			if (face == Direction.UP)
				return STICKY_NORTH;
			if (face == Direction.DOWN)
				return STICKY_SOUTH;
		}

		if (axis == Axis.Y) {
			if (face == Direction.NORTH)
				return STICKY_NORTH;
			if (face == Direction.SOUTH)
				return STICKY_SOUTH;
			if (face == Direction.EAST)
				return STICKY_EAST;
			if (face == Direction.WEST)
				return STICKY_WEST;
		}

		if (axis == Axis.Z) {
			if (face == Direction.UP)
				return STICKY_NORTH;
			if (face == Direction.DOWN)
				return STICKY_SOUTH;
			if (face == Direction.EAST)
				return STICKY_EAST;
			if (face == Direction.WEST)
				return STICKY_WEST;
		}

		return null;
	}

}
