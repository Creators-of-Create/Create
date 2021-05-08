package com.jozufozu.flywheel.backend.core;

import net.minecraft.util.math.BlockPos;

public class CrumblingRenderer extends WorldTileRenderer<CrumblingProgram> {
	public CrumblingRenderer() {
		super(WorldContext.CRUMBLING);
	}

	@Override
	protected boolean shouldTick(BlockPos worldPos, float lookX, float lookY, float lookZ, int cX, int cY, int cZ) {
		return true;
	}
}
