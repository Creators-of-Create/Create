package com.simibubi.create.content.decoration.slidingDoor;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlidingDoorShapes {

	protected static final VoxelShape SE_AABB = Block.box(0.0D, 0.0D, -13.0D, 3.0D, 16.0D, 3.0D);
	protected static final VoxelShape ES_AABB = Block.box(-13.0D, 0.0D, 0.0D, 3.0D, 16.0D, 3.0D);
	protected static final VoxelShape NW_AABB = Block.box(13.0D, 0.0D, 13.0D, 16.0D, 16.0D, 29.0D);
	protected static final VoxelShape WN_AABB = Block.box(13.0D, 0.0D, 13.0D, 29.0D, 16.0D, 16.0D);
	protected static final VoxelShape SW_AABB = Block.box(13.0D, 0.0D, -13.0D, 16.0D, 16.0D, 3.0D);
	protected static final VoxelShape WS_AABB = Block.box(13.0D, 0.0D, 0.0D, 29.0D, 16.0D, 3.0D);
	protected static final VoxelShape NE_AABB = Block.box(0.0D, 0.0D, 13.0D, 3.0D, 16.0D, 29.0D);
	protected static final VoxelShape EN_AABB = Block.box(-13.0D, 0.0D, 13.0D, 3.0D, 16.0D, 16.0D);

	protected static final VoxelShape SE_AABB_FOLD = Block.box(0.0D, 0.0D, -3.0D, 9.0D, 16.0D, 3.0D);
	protected static final VoxelShape ES_AABB_FOLD = Block.box(-3.0D, 0.0D, 0.0D, 3.0D, 16.0D, 9.0D);
	protected static final VoxelShape NW_AABB_FOLD = Block.box(7.0D, 0.0D, 13.0D, 16.0D, 16.0D, 19.0D);
	protected static final VoxelShape WN_AABB_FOLD = Block.box(13.0D, 0.0D, 7.0D, 19.0D, 16.0D, 16.0D);
	protected static final VoxelShape SW_AABB_FOLD = Block.box(7.0D, 0.0D, -3.0D, 16.0D, 16.0D, 3.0D);
	protected static final VoxelShape WS_AABB_FOLD = Block.box(13.0D, 0.0D, 0.0D, 19.0D, 16.0D, 9.0D);
	protected static final VoxelShape NE_AABB_FOLD = Block.box(0.0D, 0.0D, 13.0D, 9.0D, 16.0D, 19.0D);
	protected static final VoxelShape EN_AABB_FOLD = Block.box(-3.0D, 0.0D, 7.0D, 3.0D, 16.0D, 16.0D);

	public static VoxelShape get(Direction facing, boolean hinge, boolean fold) {
		if (fold)
			return switch (facing) {
			case SOUTH -> (hinge ? ES_AABB_FOLD : WS_AABB_FOLD);
			case WEST -> (hinge ? SW_AABB_FOLD : NW_AABB_FOLD);
			case NORTH -> (hinge ? WN_AABB_FOLD : EN_AABB_FOLD);
			default -> (hinge ? NE_AABB_FOLD : SE_AABB_FOLD);
			};

		return switch (facing) {
		case SOUTH -> (hinge ? ES_AABB : WS_AABB);
		case WEST -> (hinge ? SW_AABB : NW_AABB);
		case NORTH -> (hinge ? WN_AABB : EN_AABB);
		default -> (hinge ? NE_AABB : SE_AABB);
		};
	}

}
