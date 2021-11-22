package com.simibubi.create.foundation.utility;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;

// TODO 1.18: remove and replace all usages with ResourceManagerReloadListener
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
