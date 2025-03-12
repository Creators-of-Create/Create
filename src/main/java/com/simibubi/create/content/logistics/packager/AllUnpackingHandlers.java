package com.simibubi.create.content.logistics.packager;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.api.packager.unpacking.UnpackingHandler;
import com.simibubi.create.api.packager.unpacking.VoidingUnpackingHandler;
import com.simibubi.create.impl.unpacking.BasinUnpackingHandler;
import com.simibubi.create.impl.unpacking.CrafterUnpackingHandler;

public class AllUnpackingHandlers {
	public static void registerDefaults() {
		UnpackingHandler.REGISTRY.register(AllBlocks.BASIN.get(), BasinUnpackingHandler.INSTANCE);
		UnpackingHandler.REGISTRY.register(AllBlocks.CREATIVE_CRATE.get(), VoidingUnpackingHandler.INSTANCE);
		UnpackingHandler.REGISTRY.register(AllBlocks.MECHANICAL_CRAFTER.get(), CrafterUnpackingHandler.INSTANCE);
	}
}
