package com.simibubi.create;

import net.createmod.catnip.render.BindableTexture;
import net.minecraft.resources.ResourceLocation;

public enum AllSpecialTextures implements BindableTexture {

	CHECKERED("checkerboard.png"),
	THIN_CHECKERED("thin_checkerboard.png"),
	CUTOUT_CHECKERED("cutout_checkerboard.png"),
	HIGHLIGHT_CHECKERED("highlighted_checkerboard.png"),
	SELECTION("selection.png"),
	GLUE("glue.png"),

	;

	public static final String ASSET_PATH = "textures/special/";
	private final ResourceLocation location;

	AllSpecialTextures(String filename) {
		location = Create.asResource(ASSET_PATH + filename);
	}

	public ResourceLocation getLocation() {
		return location;
	}

}
