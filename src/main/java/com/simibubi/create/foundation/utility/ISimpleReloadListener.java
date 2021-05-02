package com.simibubi.create.foundation.utility;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Unit;

@FunctionalInterface
public interface ISimpleReloadListener extends IFutureReloadListener {

	@Override
	default CompletableFuture<Void> reload(IFutureReloadListener.IStage stage, IResourceManager resourceManager, IProfiler prepareProfiler, IProfiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
		return stage.markCompleteAwaitingOthers(Unit.INSTANCE).thenRunAsync(() -> {
			onReload(resourceManager, applyProfiler);
		}, applyExecutor);
	}

	void onReload(IResourceManager resourceManager, IProfiler profiler);

}
