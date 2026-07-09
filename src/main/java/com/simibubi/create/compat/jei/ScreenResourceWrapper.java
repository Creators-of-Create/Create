package com.simibubi.create.compat.jei;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

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
	public void draw(GuiGraphicsExtractor graphics, int xOffset, int yOffset) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, resource.location, xOffset, yOffset, resource.getStartX(), resource.getStartY(), resource.getWidth(),
			resource.getHeight(), 256, 256);
	}

}
