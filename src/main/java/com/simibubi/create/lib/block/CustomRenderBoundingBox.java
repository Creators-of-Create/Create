package com.simibubi.create.lib.block;

import net.minecraft.world.phys.AABB;

/**
 * Nothing here is actually used, it's just in case we figure something out in the future. see WorldRendererMixin.
 */
public interface CustomRenderBoundingBox {
	default AABB getRenderBoundingBox() {
		return getInfiniteBoundingBox();
	}

	default AABB getInfiniteBoundingBox() {
		return new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
}
