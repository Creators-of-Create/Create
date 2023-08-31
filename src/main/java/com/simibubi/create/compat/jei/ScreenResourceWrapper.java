package com.simibubi.create.compat.jei;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;

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
	public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
		graphics.blit(resource.location, xOffset, yOffset, 0, resource.getStartX(), resource.getStartY(), resource.getWidth(),
			resource.getHeight(), 256, 256);
	}

}
