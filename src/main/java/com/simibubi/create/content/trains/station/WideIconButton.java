package com.simibubi.create.content.trains.station;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.widget.IconButton;

import net.createmod.catnip.api.client.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class WideIconButton extends IconButton {

	public WideIconButton(int x, int y, ScreenElement icon) {
		super(x, y, 26, 18, icon);
	}

	@Override
	protected void drawBg(GuiGraphicsExtractor graphics, AllGuiTextures button) {
		super.drawBg(graphics, button);
		graphics.blit(RenderPipelines.GUI_TEXTURED, button.location, getX() + 9, getY(), button.getStartX() + 1,
			button.getStartY(), button.getWidth() - 1, button.getHeight(), 256, 256);
	}

}
