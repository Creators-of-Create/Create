package com.simibubi.create.content.logistics.tunnel;

import static net.minecraft.world.level.block.Block.box;

import com.simibubi.create.foundation.utility.VoxelShaper;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeltTunnelShapes {

	private static VoxelShape block = box(0, -5, 0, 16, 16, 16);

	private static VoxelShaper opening = VoxelShaper.forHorizontal(box(2, -5, 14, 14, 10, 16),
			Direction.SOUTH);

	private static final VoxelShaper STRAIGHT = VoxelShaper.forHorizontalAxis(Shapes.join(block,
			Shapes.or(opening.get(Direction.SOUTH), opening.get(Direction.NORTH)), BooleanOp.NOT_SAME),
			Axis.Z),

			TEE = VoxelShaper.forHorizontal(
					Shapes.join(block, Shapes.or(opening.get(Direction.NORTH),
							opening.get(Direction.WEST), opening.get(Direction.EAST)), BooleanOp.NOT_SAME),
					Direction.SOUTH);

	private static final VoxelShape CROSS = Shapes.join(block,
			Shapes.or(opening.get(Direction.SOUTH), opening.get(Direction.NORTH), opening.get(Direction.WEST),
					opening.get(Direction.EAST)),
			BooleanOp.NOT_SAME);

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
		return Shapes.block();
	}
}
