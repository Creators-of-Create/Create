package com.simibubi.create;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

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
		RenderSystem.setShaderTexture(0, location);
//		Minecraft.getInstance()
//			.getTextureManager()
//				.bindForSetup(location); // PORT: i dont know which one fits best here.
	}

	public ResourceLocation getLocation() {
		return location;
	}

}
