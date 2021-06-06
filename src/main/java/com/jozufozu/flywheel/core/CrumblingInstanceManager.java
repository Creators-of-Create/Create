package com.jozufozu.flywheel.core;

import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.TileInstanceManager;

import net.minecraft.util.math.BlockPos;

public class CrumblingInstanceManager extends TileInstanceManager {
	public CrumblingInstanceManager() {
		super(new MaterialManager<>(Contexts.CRUMBLING));
	}

	@Override
	protected boolean shouldFrameUpdate(BlockPos worldPos, float lookX, float lookY, float lookZ, int cX, int cY, int cZ) {
		return true;
	}
}
