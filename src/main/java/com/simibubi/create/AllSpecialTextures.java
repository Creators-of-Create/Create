package com.simibubi.create;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.resources.ResourceLocation;

public enum AllSpecialTextures {

	BLANK("blank.png"),
	CHECKERED("checkerboard.png"),
	THIN_CHECKERED("thin_checkerboard.png"),
	CUTOUT_CHECKERED("cutout_checkerboard.png"),
	HIGHLIGHT_CHECKERED("highlighted_checkerboard.png"),
	SELECTION("selection.png"),
	GLUE("glue.png"),

	;

	public static final String ASSET_PATH = "textures/special/";
	private ResourceLocation location;

	private AllSpecialTextures(String filename) {
		location = Create.asResource(ASSET_PATH + filename);
	}

	public void bind() {
		RenderSystem.setShaderTexture(0, location);
	}

	public ResourceLocation getLocation() {
		return location;
	}

}