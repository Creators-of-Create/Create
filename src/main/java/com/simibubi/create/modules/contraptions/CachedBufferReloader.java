package com.simibubi.create.modules.contraptions;

import com.simibubi.create.foundation.utility.ColoredIndicatorRenderer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.constructs.MechanicalBearingTileEntityRenderer;
import com.simibubi.create.modules.contraptions.receivers.constructs.MechanicalPistonTileEntityRenderer;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;

public class CachedBufferReloader extends ReloadListener<String> {

	@Override
	protected String prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
		return "";
	}

	@Override
	protected void apply(String splashList, IResourceManager resourceManagerIn, IProfiler profilerIn) {
		KineticTileEntityRenderer.invalidateCache();
		MechanicalPistonTileEntityRenderer.invalidateCache();
		MechanicalBearingTileEntityRenderer.invalidateCache();
		ColoredIndicatorRenderer.invalidateCache();
	}


}
