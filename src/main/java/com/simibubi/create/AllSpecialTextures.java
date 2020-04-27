package com.simibubi.create;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public enum AllSpecialTextures {

	BLANK("blank.png"),
	CHECKERED("checkerboard.png"),
	HIGHLIGHT_CHECKERED("highlighted_checkerboard.png"),
	SELECTION("selection.png"),
	
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