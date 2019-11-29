package com.simibubi.create.modules.contraptions.relays.belt;

import static com.simibubi.create.foundation.utility.VoxelShaper.forHorizontalAxis;
import static net.minecraft.block.Block.makeCuboidShape;
import static net.minecraft.util.math.shapes.VoxelShapes.or;

import com.simibubi.create.foundation.utility.VoxelShaper;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class BeltTunnelShapes {

	private static final VoxelShape TOP = makeCuboidShape(0, 8, 0, 16, 16, 16),
			INNER = makeCuboidShape(2, -5, 2, 14, 16, 14);

	private static final VoxelShaper WALL = VoxelShaper.forHorizontal(makeCuboidShape(0, -5, 14, 16, 16, 16)),
			POLES = VoxelShaper
					.forHorizontal(or(makeCuboidShape(0, -5, 14, 2, 16, 16), makeCuboidShape(14, -5, 14, 16, 16, 16)));

	private static final VoxelShaper STRAIGHT = forHorizontalAxis(
			VoxelShapes.or(TOP, WALL.get(Direction.EAST), WALL.get(Direction.WEST))),
			T_LEFT = forHorizontalAxis(VoxelShapes.or(TOP, WALL.get(Direction.EAST), POLES.get(Direction.WEST))),
			T_RIGHT = forHorizontalAxis(VoxelShapes.or(TOP, POLES.get(Direction.EAST), WALL.get(Direction.WEST))),
			CROSS = forHorizontalAxis(VoxelShapes.or(TOP, POLES.get(Direction.EAST), POLES.get(Direction.WEST)));

	public static VoxelShape getFrameShape(BlockState state) {
		VoxelShaper shaper = null;
		switch (state.get(BeltTunnelBlock.SHAPE)) {
		case CROSS:
			shaper = CROSS;
			break;
		case T_LEFT:
			shaper = T_LEFT;
			break;
		case T_RIGHT:
			shaper = T_RIGHT;
			break;
		case STRAIGHT:
		case WINDOW:
		default:
			shaper = STRAIGHT;
			break;
		}
		return shaper.get(state.get(BeltTunnelBlock.HORIZONTAL_AXIS));
	}

	public static VoxelShape getFilledShape(BlockState state) {
		return or(getFrameShape(state), INNER);
	}

}
