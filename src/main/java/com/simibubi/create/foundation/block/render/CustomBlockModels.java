package com.simibubi.create.foundation.block.render;

import java.util.IdentityHashMap;
import java.util.Map;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class CustomBlockModels {

	private final Multimap<ResourceLocation, NonNullFunction<BakedModel, ? extends BakedModel>> modelFuncs = MultimapBuilder.hashKeys().arrayListValues().build();
	private final Map<Block, NonNullFunction<BakedModel, ? extends BakedModel>> finalModelFuncs = new IdentityHashMap<>();
	private boolean funcsLoaded = false;

	public void register(ResourceLocation block, NonNullFunction<BakedModel, ? extends BakedModel> func) {
		modelFuncs.put(block, func);
	}

	public void forEach(NonNullBiConsumer<Block, NonNullFunction<BakedModel, ? extends BakedModel>> consumer) {
		loadEntriesIfMissing();
		finalModelFuncs.forEach(consumer);
	}

	private void loadEntriesIfMissing() {
		if (!funcsLoaded) {
			loadEntries();
			funcsLoaded = true;
		}
	}

	private void loadEntries() {
		finalModelFuncs.clear();
		modelFuncs.asMap().forEach((location, funcList) -> {
			Block block = ForgeRegistries.BLOCKS.getValue(location);
			if (block == null) {
				return;
			}

			NonNullFunction<BakedModel, ? extends BakedModel> finalFunc = null;
			for (NonNullFunction<BakedModel, ? extends BakedModel> func : funcList) {
				if (finalFunc == null) {
					finalFunc = func;
				} else {
					finalFunc = finalFunc.andThen(func);
				}
			}

			finalModelFuncs.put(block, finalFunc);
		});
	}

}
