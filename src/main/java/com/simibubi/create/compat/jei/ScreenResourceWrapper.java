package com.simibubi.create.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiComponent;

public class ScreenResourceWrapper implements IDrawable {

	private AllGuiTextures resource;

	public ScreenResourceWrapper(AllGuiTextures resource) {
		this.resource = resource;
	}

	@Override
	public int getWidth() {
		return resource.getWidth();
	}

	@Override
	public int getHeight() {
		return resource.getHeight();
	}

	@Override
	public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
		resource.bind();
		GuiComponent.blit(matrixStack, xOffset, yOffset, 0, resource.getStartX(), resource.getStartY(), resource.getWidth(),
				resource.getHeight(), 256, 256);
	}

}
