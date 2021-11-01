package com.simibubi.create.foundation.block.render;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.world.level.block.Block;
import net.minecraft.client.resources.model.BakedModel;

public class CustomBlockModels {

	private List<Pair<Supplier<? extends Block>, NonNullFunction<BakedModel, ? extends BakedModel>>> registered;
	private Map<Block, NonNullFunction<BakedModel, ? extends BakedModel>> customModels;

	public CustomBlockModels() {
		registered = new ArrayList<>();
		customModels = new IdentityHashMap<>();
	}

	public void register(Supplier<? extends Block> entry,
		NonNullFunction<BakedModel, ? extends BakedModel> behaviour) {
		registered.add(Pair.of(entry, behaviour));
	}

	public void forEach(NonNullBiConsumer<Block, NonNullFunction<BakedModel, ? extends BakedModel>> consumer) {
		loadEntriesIfMissing();
		customModels.forEach(consumer);
	}

	private void loadEntriesIfMissing() {
		if (customModels.isEmpty())
			loadEntries();
	}

	private void loadEntries() {
		customModels.clear();
		registered.forEach(p -> {
			Block key = p.getKey()
				.get();
			
			NonNullFunction<BakedModel, ? extends BakedModel> existingModel = customModels.get(key);
			if (existingModel != null) {
				customModels.put(key, p.getValue()
					.andThen(existingModel));
				return;
			}
			
			customModels.put(key, p.getValue());
		});
	}

}
