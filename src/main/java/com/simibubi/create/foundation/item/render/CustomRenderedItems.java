package com.simibubi.create.foundation.item.render;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class CustomRenderedItems {

	private final Map<ResourceLocation, NonNullFunction<BakedModel, ? extends CustomRenderedItemModel>> modelFuncs = new HashMap<>();
	private final Map<Item, NonNullFunction<BakedModel, ? extends CustomRenderedItemModel>> finalModelFuncs = new IdentityHashMap<>();

	public void register(ResourceLocation item,
		NonNullFunction<BakedModel, ? extends CustomRenderedItemModel> func) {
		modelFuncs.put(item, func);
	}

	public void forEach(
		NonNullBiConsumer<Item, NonNullFunction<BakedModel, ? extends CustomRenderedItemModel>> consumer) {
		loadEntriesIfMissing();
		finalModelFuncs.forEach(consumer);
	}

	private void loadEntriesIfMissing() {
		if (finalModelFuncs.isEmpty())
			loadEntries();
	}

	private void loadEntries() {
		finalModelFuncs.clear();
		CustomRenderedItemModelRenderer.acceptModelFuncs(finalModelFuncs::put);
		modelFuncs.forEach((location, func) -> {
			Item item = ForgeRegistries.ITEMS.getValue(location);
			if (item == null) {
				return;
			}
			finalModelFuncs.put(item, func);
		});
	}

}
