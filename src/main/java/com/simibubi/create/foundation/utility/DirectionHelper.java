package com.simibubi.create.foundation.utility;

import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.EAST;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.core.Direction.WEST;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

/**
 * A bunch of methods that got stripped out of Direction in 1.15
 *
 * @author Mojang
 */
public class DirectionHelper {

	public static Direction rotateAround(Direction dir, Direction.Axis axis) {
		switch (axis) {
			case X -> {
				if (dir != WEST && dir != EAST) {
					return rotateX(dir);
				}
				return dir;
			}
			case Y -> {
				if (dir != UP && dir != DOWN) {
					return dir.getClockWise();
				}
				return dir;
			}
			case Z -> {
				if (dir != NORTH && dir != SOUTH) {
					return rotateZ(dir);
				}
				return dir;
			}
			default -> throw new IllegalStateException("Unable to get CW facing for axis " + axis);
		}
	}

	public static Direction rotateX(Direction dir) {
		return switch (dir) {
			case NORTH -> DOWN;
			case SOUTH -> UP;
			case UP -> NORTH;
			case DOWN -> SOUTH;
			default -> throw new IllegalStateException("Unable to get X-rotated facing of " + dir);
		};
	}

	public static Direction rotateZ(Direction dir) {
		return switch (dir) {
			case EAST -> DOWN;
			case WEST -> UP;
			case UP -> EAST;
			case DOWN -> WEST;
			default -> throw new IllegalStateException("Unable to get Z-rotated facing of " + dir);
		};
	}

	public static Direction getPositivePerpendicular(Axis horizontalAxis) {
		return horizontalAxis == Axis.X ? SOUTH : EAST;
	}

}
