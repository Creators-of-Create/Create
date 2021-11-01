package com.simibubi.create.foundation.utility;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;

@FunctionalInterface
public interface ISimpleReloadListener extends PreparableReloadListener {

	@Override
	default CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier stage, ResourceManager resourceManager, ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
		return stage.wait(Unit.INSTANCE).thenRunAsync(() -> {
			onReload(resourceManager, applyProfiler);
		}, applyExecutor);
	}

	void onReload(ResourceManager resourceManager, ProfilerFiller profiler);

}
