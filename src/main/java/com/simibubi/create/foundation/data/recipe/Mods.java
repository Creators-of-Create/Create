package com.simibubi.create.foundation.data.recipe;

import net.minecraft.resources.ResourceLocation;

public enum Mods {

	MEK("mekanism", true), 
	TH("thermal", false), 
	MW("mysticalworld", false), 
	SM("silents_mechanisms", false), 
	IE("immersiveengineering", true),
	EID("eidolon", false),
	INF("iceandfire", false)

	;

	private String id;
	private boolean reversedPrefix;

	private Mods(String id, boolean reversedPrefix) {
		this.id = id;
		this.reversedPrefix = reversedPrefix;}

	public ResourceLocation ingotOf(String type) {
		return new ResourceLocation(id, reversedPrefix ? "ingot_" + type : type + "_ingot");
	}
	
	public ResourceLocation nuggetOf(String type) {
		return new ResourceLocation(id, reversedPrefix ? "nugget_" + type : type + "_nugget");
	}
	
	public String getId() {
		return id;
	}
	
}
