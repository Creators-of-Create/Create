package com.simibubi.create.foundation.gui.widget;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.element.ScreenElement;

import net.minecraft.network.chat.Component;

public class IconButton extends AbstractSimiWidget {

	protected ScreenElement icon;

	public IconButton(int x, int y, ScreenElement icon) {
		this(x, y, 18, 18, icon);
	}
	
	public IconButton(int x, int y, int w, int h, ScreenElement icon) {
		super(x, y, w, h);
		this.icon = icon;
	}

	@Override
	public void renderButton(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

			AllGuiTextures button = !active ? AllGuiTextures.BUTTON_DOWN
				: isHoveredOrFocused() ? AllGuiTextures.BUTTON_HOVER : AllGuiTextures.BUTTON;

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			drawBg(matrixStack, button);
			icon.render(matrixStack, x + 1, y + 1);
		}
	}

	protected void drawBg(PoseStack matrixStack, AllGuiTextures button) {
		AllGuiTextures.BUTTON.bind();
		blit(matrixStack, x, y, button.startX, button.startY, button.width, button.height);
	}

	public void setToolTip(Component text) {
		toolTip.clear();
		toolTip.add(text);
	}

	public void setIcon(ScreenElement icon) {
		this.icon = icon;
	}
}
