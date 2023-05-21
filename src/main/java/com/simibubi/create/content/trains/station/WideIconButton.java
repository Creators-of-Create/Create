package com.simibubi.create.content.trains.station;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.ScreenElement;
import com.simibubi.create.foundation.gui.widget.IconButton;

public class WideIconButton extends IconButton {

	public WideIconButton(int x, int y, ScreenElement icon) {
		super(x, y, 26, 18, icon);
	}

	@Override
	protected void drawBg(PoseStack matrixStack, AllGuiTextures button) {
		super.drawBg(matrixStack, button);
		blit(matrixStack, x + 9, y, button.startX + 1, button.startY, button.width - 1, button.height);
	}

}
