package com.simibubi.create.foundation.utility;

import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import static net.minecraft.block.Block.makeCuboidShape;

public class VoxelShapers {

	public static final VoxelShape
		LOGISTICAL_CASING_SINGLE_SHAPE = VoxelShapes.or(
			makeCuboidShape(0, 0, 0, 16, 2, 16),
			makeCuboidShape(1, 1, 1, 15, 15, 15),
			makeCuboidShape(0, 14, 0, 16, 16, 16))

				;

	private static final VoxelShape
		LOGISTICAL_CASING_MIDDLE_SHAPE = VoxelShapes.or(
			makeCuboidShape(1,0,1,15,16,15),
			makeCuboidShape(0,0,0,2,16,2),
			makeCuboidShape(14,0,0,16,16,2),
			makeCuboidShape(0,0,14,2,16,16),
			makeCuboidShape(14,0,14,16,16,16)),

		LOGISTICAL_CASING_CAP_SHAPE = VoxelShapes.or(
			LOGISTICAL_CASING_MIDDLE_SHAPE,
			makeCuboidShape(0,0,0,16,2,16))

				;




	public static final VoxelShaper
		SHORT_CASING = VoxelShaper.forDirectional(makeCuboidShape(0, 0, 0, 16, 12, 16), Direction.UP),
		LOGISTICAL_CASING_MIDDLE = VoxelShaper.forDirectional(LOGISTICAL_CASING_MIDDLE_SHAPE, Direction.UP),
		LOGISTICAL_CASING_CAP = VoxelShaper.forDirectional(LOGISTICAL_CASING_CAP_SHAPE, Direction.UP),
		HARVESTER_BASE = VoxelShaper.forHorizontal(makeCuboidShape(0, 2, 0, 16, 14, 3), Direction.SOUTH)

				;


}
