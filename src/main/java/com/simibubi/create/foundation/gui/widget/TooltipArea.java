package com.simibubi.create.foundation.gui.widget;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;

public class TooltipArea extends AbstractSimiWidget {

	public TooltipArea(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	@Override
	public void renderButton(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		if (visible)
			isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
	}

	public TooltipArea withTooltip(List<Component> tooltip) {
		this.toolTip = tooltip;
		return this;
	}

}
