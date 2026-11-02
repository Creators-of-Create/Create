package com.simibubi.create.foundation.utility;

import net.createmod.catnip.gui.TextureSheetSegment;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public interface GuiRenderableTexture extends ScreenElement, TextureSheetSegment {
    void render(GuiGraphics graphics, int x, int y);
    
	int getStartX();

	int getStartY();

	int getWidth();

	int getHeight();
	
    public ResourceLocation getLocation();
}
