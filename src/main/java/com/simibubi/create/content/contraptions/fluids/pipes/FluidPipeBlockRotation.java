package com.simibubi.create.content.contraptions.fluids.pipes;

import java.util.Map;

import com.simibubi.create.content.contraptions.components.structureMovement.StructureTransform;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class FluidPipeBlockRotation {

	public static final Map<Direction, BooleanProperty> FACING_TO_PROPERTY_MAP = PipeBlock.PROPERTY_BY_DIRECTION;

	public static BlockState rotate(BlockState state, Rotation rotation) {
		BlockState rotated = state;
		for (Direction direction : Iterate.horizontalDirections)
			rotated = rotated.setValue(FACING_TO_PROPERTY_MAP.get(rotation.rotate(direction)),
				state.getValue(FACING_TO_PROPERTY_MAP.get(direction)));
		return rotated;
	}

	public static BlockState mirror(BlockState state, Mirror mirror) {
		BlockState mirrored = state;
		for (Direction direction : Iterate.horizontalDirections)
			mirrored = mirrored.setValue(FACING_TO_PROPERTY_MAP.get(mirror.mirror(direction)),
				state.getValue(FACING_TO_PROPERTY_MAP.get(direction)));
		return mirrored;
	}

	public static BlockState transform(BlockState state, StructureTransform transform) {
		if (transform.mirror != null)
			state = mirror(state, transform.mirror);

		if (transform.rotationAxis == Direction.Axis.Y)
			return rotate(state, transform.rotation);

		BlockState rotated = state;
		for (Direction direction : Iterate.directions)
			rotated = rotated.setValue(FACING_TO_PROPERTY_MAP.get(transform.rotateFacing(direction)),
				state.getValue(FACING_TO_PROPERTY_MAP.get(direction)));
		return rotated;
	}

}
