package com.simibubi.create.content.logistics.block.chute;

import java.util.HashMap;
import java.util.Map;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.logistics.block.chute.ChuteBlock.Shape;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class ChuteShapes {

	static Map<BlockState, VoxelShape> cache = new HashMap<>();
	static Map<BlockState, VoxelShape> collisionCache = new HashMap<>();

	public static final VoxelShape INTERSECTION_MASK = Block.box(0, -16, 0, 16, 16, 16);
	public static final VoxelShape COLLISION_MASK = Block.box(0, 0, 0, 16, 24, 16);

	public static VoxelShape createShape(BlockState state) {
		if (AllBlocks.SMART_CHUTE.has(state))
			return AllShapes.SMART_CHUTE;
		
		Direction direction = state.getValue(ChuteBlock.FACING);
		Shape shape = state.getValue(ChuteBlock.SHAPE);

		boolean intersection = shape == Shape.INTERSECTION;
		if (direction == Direction.DOWN)
			return intersection ? VoxelShapes.block() : AllShapes.CHUTE;

		VoxelShape combineWith = intersection ? VoxelShapes.block() : VoxelShapes.empty();
		VoxelShape result = VoxelShapes.or(combineWith, AllShapes.CHUTE_SLOPE.get(direction));
		if (intersection)
			result = VoxelShapes.joinUnoptimized(INTERSECTION_MASK, result, IBooleanFunction.AND);
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
		VoxelShape createdShape = VoxelShapes.joinUnoptimized(COLLISION_MASK, getShape(state), IBooleanFunction.AND);
		collisionCache.put(state, createdShape);
		return createdShape;
	}

	public static final VoxelShape PANEL = Block.box(1, -15, 0, 15, 4, 1);

	public static VoxelShape createSlope() {
		VoxelShape shape = VoxelShapes.empty();
		for (int i = 0; i < 16; i++) {
			float offset = i / 16f;
			shape = VoxelShapes.join(shape, PANEL.move(0, offset, offset), IBooleanFunction.OR);
		}
		return shape;
	}

}
