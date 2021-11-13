package com.simibubi.create.lib.event;

import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public interface ModelsBakedCallback {
	Event<ModelsBakedCallback> EVENT = EventFactory.createArrayBacked(ModelsBakedCallback.class, callbacks -> (manager, models, loader) -> {
		for (ModelsBakedCallback callback : callbacks) {
			callback.onModelsBaked(manager, models, loader);
		}
	});

	void onModelsBaked(ModelManager manager, Map<ResourceLocation, BakedModel> models, ModelBakery loader);
}
