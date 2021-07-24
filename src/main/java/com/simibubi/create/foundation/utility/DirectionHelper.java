package com.simibubi.create.foundation.utility;

import static net.minecraft.util.Direction.DOWN;
import static net.minecraft.util.Direction.EAST;
import static net.minecraft.util.Direction.NORTH;
import static net.minecraft.util.Direction.SOUTH;
import static net.minecraft.util.Direction.UP;
import static net.minecraft.util.Direction.WEST;

import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

/**
 * A bunch of methods that got stripped out of Direction in 1.15
 * 
 * @author Mojang
 */
public class DirectionHelper {

	public static Direction rotateAround(Direction dir, Direction.Axis axis) {
		switch (axis) {
		case X:
			if (dir != WEST && dir != EAST) {
				return rotateX(dir);
			}

			return dir;
		case Y:
			if (dir != UP && dir != DOWN) {
				return dir.getClockWise();
			}

			return dir;
		case Z:
			if (dir != NORTH && dir != SOUTH) {
				return rotateZ(dir);
			}

			return dir;
		default:
			throw new IllegalStateException("Unable to get CW facing for axis " + axis);
		}
	}

	public static Direction rotateX(Direction dir) {
		switch (dir) {
		case NORTH:
			return DOWN;
		case EAST:
		case WEST:
		default:
			throw new IllegalStateException("Unable to get X-rotated facing of " + dir);
		case SOUTH:
			return UP;
		case UP:
			return NORTH;
		case DOWN:
			return SOUTH;
		}
	}

	public static Direction rotateZ(Direction dir) {
		switch (dir) {
		case EAST:
			return DOWN;
		case SOUTH:
		default:
			throw new IllegalStateException("Unable to get Z-rotated facing of " + dir);
		case WEST:
			return UP;
		case UP:
			return EAST;
		case DOWN:
			return WEST;
		}
	}

	public static Direction getPositivePerpendicular(Axis horizontalAxis) {
		return horizontalAxis == Axis.X ? SOUTH : EAST;
	}

}
