package com.simibubi.create.content.logistics.chute;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.logistics.chute.ChuteBlock.Shape;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChuteShapes {

	static Map<BlockState, VoxelShape> cache = new HashMap<>();
	static Map<BlockState, VoxelShape> collisionCache = new HashMap<>();

	public static final VoxelShape INTERSECTION_MASK = Block.box(0, -16, 0, 16, 16, 16);
	public static final VoxelShape COLLISION_MASK = Block.box(0, 0, 0, 16, 24, 16);

	public static VoxelShape createShape(BlockState state) {
		if (AllBlocks.SMART_CHUTE.has(state))
			return Shapes.block();
		
		Direction direction = state.getValue(ChuteBlock.FACING);
		Shape shape = state.getValue(ChuteBlock.SHAPE);

		boolean intersection = shape == Shape.INTERSECTION || shape == Shape.ENCASED;
		if (direction == Direction.DOWN)
			return intersection ? Shapes.block() : AllShapes.CHUTE;

		VoxelShape combineWith = intersection ? Shapes.block() : Shapes.empty();
		VoxelShape result = Shapes.or(combineWith, AllShapes.CHUTE_SLOPE.get(direction));
		if (intersection)
			result = Shapes.joinUnoptimized(INTERSECTION_MASK, result, BooleanOp.AND);
		return result;
	}

	public static VoxelShape getShape(BlockState state) {
		if (cache.containsKey(state))
			return cache.get(state);
		VoxelShape createdShape = createShape(state);
		cache.put(state, createdShape);
		return createdShape;
	}

	public static VoxelShape getCollisionShape(BlockState state) {
		if (collisionCache.containsKey(state))
			return collisionCache.get(state);
		VoxelShape createdShape = Shapes.joinUnoptimized(COLLISION_MASK, getShape(state), BooleanOp.AND);
		collisionCache.put(state, createdShape);
		return createdShape;
	}

	public static final VoxelShape PANEL = Block.box(1, -15, 0, 15, 4, 1);

	public static VoxelShape createSlope() {
		VoxelShape shape = Shapes.empty();
		for (int i = 0; i < 16; i++) {
			float offset = i / 16f;
			shape = Shapes.join(shape, PANEL.move(0, offset, offset), BooleanOp.OR);
		}
		return shape;
	}

}
