package com.simibubi.create;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public enum AllSpecialTextures {

	Selection("selection.png"),
	
	;

	public static final String ASSET_PATH = "textures/special/";
	private ResourceLocation location;

	private AllSpecialTextures(String filename) {
		location = new ResourceLocation(Create.ID, ASSET_PATH + filename);
	}

	public void bind() {
		Minecraft.getInstance().getTextureManager().bindTexture(location);
	}

}