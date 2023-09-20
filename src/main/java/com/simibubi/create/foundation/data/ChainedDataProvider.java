package com.simibubi.create.foundation.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

@Deprecated(forRemoval = true)
public class ChainedDataProvider implements DataProvider {

	private DataProvider mainProvider;
	private DataProvider addedProvider;

	public ChainedDataProvider(DataProvider mainProvider, DataProvider addedProvider) {
		this.mainProvider = mainProvider;
		this.addedProvider = addedProvider;
	}
	
	@Override
	public CompletableFuture<?> run(CachedOutput pOutput) {
		return mainProvider.run(pOutput)
			.thenCompose(s -> addedProvider.run(pOutput));
	}

	@Override
	public String getName() {
		return mainProvider.getName() + " with " + addedProvider.getName();
	}

}
