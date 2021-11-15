package com.simibubi.create.foundation.gui.widget;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;

import net.minecraft.network.chat.Component;

public class IconButton extends AbstractSimiWidget {

	private AllIcons icon;

	public IconButton(int x, int y, AllIcons icon) {
		super(x, y, 18, 18);
		this.icon = icon;
	}

	@Override
	public void renderButton(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

			AllGuiTextures button = !active ? AllGuiTextures.BUTTON_DOWN
				: isHovered() ? AllGuiTextures.BUTTON_HOVER : AllGuiTextures.BUTTON;

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			AllGuiTextures.BUTTON.bind();
			blit(matrixStack, x, y, button.startX, button.startY, button.width, button.height);
			icon.render(matrixStack, x + 1, y + 1, this);
		}
	}

	public void setToolTip(Component text) {
		toolTip.clear();
		toolTip.add(text);
	}

	public void setIcon(AllIcons icon) {
		this.icon = icon;
	}
}
