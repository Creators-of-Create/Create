package com.simibubi.create.foundation.gui.widgets;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;

import net.minecraft.util.text.ITextComponent;

public class IconButton extends AbstractSimiWidget {

	private AllIcons icon;
	protected boolean pressed;

	public IconButton(int x, int y, AllIcons icon) {
		super(x, y, 18, 18);
		this.icon = icon;
	}

	@Override
	public void renderButton(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			this.isHovered =
				mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

			AllGuiTextures button = (pressed || !active) ? button = AllGuiTextures.BUTTON_DOWN
				: (isHovered) ? AllGuiTextures.BUTTON_HOVER : AllGuiTextures.BUTTON;

			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			AllGuiTextures.BUTTON.bind();
			blit(matrixStack, x, y, button.startX, button.startY, button.width, button.height);
			icon.draw(matrixStack, this, x + 1, y + 1);
		}
	}

	@Override
	public void onClick(double p_onClick_1_, double p_onClick_3_) {
		super.onClick(p_onClick_1_, p_onClick_3_);
		this.pressed = true;
	}

	@Override
	public void onRelease(double p_onRelease_1_, double p_onRelease_3_) {
		super.onRelease(p_onRelease_1_, p_onRelease_3_);
		this.pressed = false;
	}

	public void setToolTip(ITextComponent text) {
		toolTip.clear();
		toolTip.add(text);
	}

	public void setIcon(AllIcons icon) {
		this.icon = icon;
	}
}
