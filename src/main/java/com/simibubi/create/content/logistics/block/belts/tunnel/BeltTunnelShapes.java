package com.simibubi.create.content.logistics.block.belts.tunnel;


import static net.minecraft.block.Block.box;

import com.simibubi.create.foundation.utility.VoxelShaper;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class BeltTunnelShapes {

	private static VoxelShape block = box(0, -5, 0, 16, 16, 16);

	private static VoxelShaper opening = VoxelShaper.forHorizontal(box(2, -5, 14, 14, 10, 16),
			Direction.SOUTH);

	private static final VoxelShaper STRAIGHT = VoxelShaper.forHorizontalAxis(VoxelShapes.join(block,
			VoxelShapes.or(opening.get(Direction.SOUTH), opening.get(Direction.NORTH)), IBooleanFunction.NOT_SAME),
			Axis.Z),

			TEE = VoxelShaper.forHorizontal(
					VoxelShapes.join(block, VoxelShapes.or(opening.get(Direction.NORTH),
							opening.get(Direction.WEST), opening.get(Direction.EAST)), IBooleanFunction.NOT_SAME),
					Direction.SOUTH);

	private static final VoxelShape CROSS = VoxelShapes.join(block,
			VoxelShapes.or(opening.get(Direction.SOUTH), opening.get(Direction.NORTH), opening.get(Direction.WEST),
					opening.get(Direction.EAST)),
			IBooleanFunction.NOT_SAME);

	public static VoxelShape getShape(BlockState state) {
		BeltTunnelBlock.Shape shape = state.getValue(BeltTunnelBlock.SHAPE);
		Direction.Axis axis = state.getValue(BeltTunnelBlock.HORIZONTAL_AXIS);

		if (shape == BeltTunnelBlock.Shape.CROSS)
			return CROSS;

		if (BeltTunnelBlock.isStraight(state))
			return STRAIGHT.get(axis);

		if (shape == BeltTunnelBlock.Shape.T_LEFT)
			return TEE.get(axis == Direction.Axis.Z ? Direction.EAST : Direction.NORTH);

		if (shape == BeltTunnelBlock.Shape.T_RIGHT)
			return TEE.get(axis == Direction.Axis.Z ? Direction.WEST : Direction.SOUTH);

		// something went wrong
		return VoxelShapes.block();
	}
}
