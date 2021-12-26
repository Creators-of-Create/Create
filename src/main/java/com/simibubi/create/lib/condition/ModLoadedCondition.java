package com.simibubi.create.lib.condition;

import com.simibubi.create.lib.data.ICondition;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

public class ModLoadedCondition implements ICondition {
	private static final ResourceLocation NAME = new ResourceLocation("create", "mod_loaded");
	private final String modid;
	private final boolean loaded;

	public ModLoadedCondition(String modid) {
		this.modid = modid;
		this.loaded = FabricLoader.getInstance().isModLoaded(modid);
	}

	@Override
	public String toString() {
		return "mod_loaded(\"" + modid + "\")";
	}

	@Override
	public ResourceLocation getID() {
		return NAME;
	}

	@Override
	public boolean test() {
		return loaded;
	}
}
