package com.simibubi.create.foundation.config.ui;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;

public class HintableTextFieldWidget extends EditBox {

	protected Font font;
	protected String hint;

	public HintableTextFieldWidget(Font font, int x, int y, int width, int height) {
		super(font, x, y, width, height, Components.immutableEmpty());
		this.font = font;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWidget(graphics, mouseX, mouseY, partialTicks);

		if (hint == null || hint.isEmpty())
			return;

		if (!getValue().isEmpty())
			return;

		graphics.drawString(font, hint, getX() + 5, this.getY() + (this.height - 8) / 2, Theme.c(Theme.Key.TEXT).scaleAlpha(.75f).getRGB(), false);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		if (!isMouseOver(x, y))
			return false;

		if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			setValue("");
			return true;
		} else
			return super.mouseClicked(x, y, button);
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		InputConstants.Key mouseKey = InputConstants.getKey(code, p_keyPressed_2_);
		if (Minecraft.getInstance().options.keyInventory.isActiveAndMatches(mouseKey)) {
			return true;
		}

		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
	}
}
