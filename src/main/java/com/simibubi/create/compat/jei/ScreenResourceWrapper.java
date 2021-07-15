package com.simibubi.create.compat.jei;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.AbstractGui;

public class ScreenResourceWrapper implements IDrawable {

	private AllGuiTextures resource;

	public ScreenResourceWrapper(AllGuiTextures resource) {
		this.resource = resource;
	}

	@Override
	public int getWidth() {
		return resource.width;
	}

	@Override
	public int getHeight() {
		return resource.height;
	}

	@Override
	public void draw(MatrixStack matrixStack, int xOffset, int yOffset) {
		resource.bind();
		AbstractGui.blit(matrixStack, xOffset, yOffset, 0, resource.startX, resource.startY, resource.width,
			resource.height, 256, 256);
	}

}
