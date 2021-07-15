package com.simibubi.create;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public enum AllSpecialTextures {

	BLANK("blank.png"),
	CHECKERED("checkerboard.png"),
	THIN_CHECKERED("thin_checkerboard.png"),
	CUTOUT_CHECKERED("cutout_checkerboard.png"),
	HIGHLIGHT_CHECKERED("highlighted_checkerboard.png"),
	SELECTION("selection.png"),

	;

	public static final String ASSET_PATH = "textures/special/";
	private ResourceLocation location;

	private AllSpecialTextures(String filename) {
		location = new ResourceLocation(Create.ID, ASSET_PATH + filename);
	}

	public void bind() {
		Minecraft.getInstance()
			.getTextureManager()
			.bind(location);
	}

	public ResourceLocation getLocation() {
		return location;
	}

}