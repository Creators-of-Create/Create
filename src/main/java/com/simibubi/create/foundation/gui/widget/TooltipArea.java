package com.simibubi.create.foundation.gui.widget;

import java.util.List;

import net.createmod.catnip.api.client.gui.widget.AbstractSimiWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class TooltipArea extends AbstractSimiWidget {

	public TooltipArea(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	@Override
	protected void doRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		if (visible)
			isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
	}

	public TooltipArea withTooltip(List<Component> tooltip) {
		this.toolTip = tooltip;
		return this;
	}

}
