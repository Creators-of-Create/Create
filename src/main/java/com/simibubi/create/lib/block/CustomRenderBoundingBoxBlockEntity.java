package com.simibubi.create.lib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CustomRenderBoundingBoxBlockEntity {
	AABB INFINITE_EXTENT_AABB = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

	default BlockEntity self() {
		return (BlockEntity) this;
	}

	default AABB getRenderBoundingBox() {
		AABB box = getInfiniteBoundingBox();
		BlockPos pos = self().getBlockPos();
		try {
			VoxelShape collisionShape = self().getBlockState().getCollisionShape(self().getLevel(), pos);
			if (!collisionShape.isEmpty()) {
				box = collisionShape.bounds().move(pos);
			}
		} catch (Exception e) {
			box = new AABB(pos.offset(-1, 0, -1), pos.offset(1, 1, 1));
		}

		return box;
	}

	default AABB getInfiniteBoundingBox() {
		return INFINITE_EXTENT_AABB;
	}
}
