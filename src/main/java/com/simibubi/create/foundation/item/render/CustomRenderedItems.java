package com.simibubi.create.foundation.item.render;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;

public class CustomRenderedItems {

	private List<Pair<Supplier<? extends Item>, NonNullFunction<BakedModel, ? extends CustomRenderedItemModel>>> registered;
	private Map<Item, NonNullFunction<BakedModel, ? extends CustomRenderedItemModel>> customModels;

	public CustomRenderedItems() {
		registered = new ArrayList<>();
		customModels = new IdentityHashMap<>();
	}

	public void register(Supplier<? extends Item> entry,
		NonNullFunction<BakedModel, ? extends CustomRenderedItemModel> behaviour) {
		registered.add(Pair.of(entry, behaviour));
	}

	public void forEach(
		NonNullBiConsumer<Item, NonNullFunction<BakedModel, ? extends CustomRenderedItemModel>> consumer) {
		loadEntriesIfMissing();
		customModels.forEach(consumer);
	}

	private void loadEntriesIfMissing() {
		if (customModels.isEmpty())
			loadEntries();
	}

	private void loadEntries() {
		customModels.clear();
		registered.forEach(p -> customModels.put(p.getKey()
			.get(), p.getValue()));
	}

}
