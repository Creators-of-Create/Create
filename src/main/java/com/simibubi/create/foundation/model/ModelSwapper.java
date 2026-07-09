package com.simibubi.create.foundation.model;

import com.simibubi.create.foundation.block.render.CustomBlockModels;
import com.simibubi.create.foundation.item.render.CustomItemModels;

import net.neoforged.bus.api.IEventBus;

public class ModelSwapper {

	protected CustomBlockModels customBlockModels = new CustomBlockModels();
	protected CustomItemModels customItemModels = new CustomItemModels();

	public CustomBlockModels getCustomBlockModels() {
		return customBlockModels;
	}

	public CustomItemModels getCustomItemModels() {
		return customItemModels;
	}

	public void registerListeners(IEventBus modEventBus) {
		// TODO 26.2: Register custom BlockStateModel/ItemModel loaders instead of mutating baked model maps.
	}

}
