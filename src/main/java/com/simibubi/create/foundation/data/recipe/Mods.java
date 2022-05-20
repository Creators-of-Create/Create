package com.simibubi.create.foundation.data.recipe;

import net.minecraft.resources.ResourceLocation;

public enum Mods {

	MEK("mekanism", true),
	TH("thermal", false),
	MW("mysticalworld", false),
	SM("silents_mechanisms", false),
	IE("immersiveengineering", true),
	EID("eidolon", false),
	INF("iceandfire", false),

	// Fabric Mods
	TR("techreborn", false),
	MI("modern_industrialization", false),
	MTM("mythicmetals", false),
	ALG("alloygery", false)
	;

	private final String id;
	private final boolean reversedPrefix;

	private Mods(String id, boolean reversedPrefix) {
		this.id = id;
		this.reversedPrefix = reversedPrefix;}

	public ResourceLocation ingotOf(String type) {
		return new ResourceLocation(id, reversedPrefix ? "ingot_" + type : type + "_ingot");
	}

	public ResourceLocation nuggetOf(String type) {
		return new ResourceLocation(id, reversedPrefix ? "nugget_" + type : type + "_nugget");
	}

	public ResourceLocation oreOf(String type) {
		return new ResourceLocation(id, reversedPrefix ? "ore_" + type : type + "_ore");
	}

	public ResourceLocation deepslateOreOf(String type) {
		return new ResourceLocation(id, reversedPrefix ? "deepslate_ore_" + type : "deepslate_" + type + "_ore");
	}

	public String getId() {
		return id;
	}

}
