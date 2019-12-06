package com.simibubi.create;

import com.simibubi.create.foundation.block.SpriteShifter;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;

public class ResourceReloadHandler extends ReloadListener<String> {

	@Override
	protected String prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
		return "";
	}

	@Override
	protected void apply(String splashList, IResourceManager resourceManagerIn, IProfiler profilerIn) {
		SpriteShifter.reloadUVs();
		CreateClient.bufferCache.invalidate();
	}

}
