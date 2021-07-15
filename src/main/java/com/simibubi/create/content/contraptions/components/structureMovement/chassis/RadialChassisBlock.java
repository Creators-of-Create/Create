package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

import net.minecraft.block.AbstractBlock.Properties;

public class RadialChassisBlock extends AbstractChassisBlock {

	public static final BooleanProperty STICKY_NORTH = BooleanProperty.create("sticky_north");
	public static final BooleanProperty STICKY_SOUTH = BooleanProperty.create("sticky_south");
	public static final BooleanProperty STICKY_EAST = BooleanProperty.create("sticky_east");
	public static final BooleanProperty STICKY_WEST = BooleanProperty.create("sticky_west");

	public RadialChassisBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(STICKY_EAST, false).setValue(STICKY_SOUTH, false).setValue(STICKY_NORTH, false)
				.setValue(STICKY_WEST, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(STICKY_NORTH, STICKY_EAST, STICKY_SOUTH, STICKY_WEST);
		super.createBlockStateDefinition(builder);
	}

	@Override
	public BooleanProperty getGlueableSide(BlockState state, Direction face) {
		Axis axis = state.getValue(AXIS);

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
