package com.simibubi.create.foundation.config.ui;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.Theme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.StringTextComponent;

public class HintableTextFieldWidget extends TextFieldWidget {

	protected FontRenderer font;
	protected String hint;

	public HintableTextFieldWidget(FontRenderer font, int x, int y, int width, int height) {
		super(font, x, y, width, height, StringTextComponent.EMPTY);
		this.font = font;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	@Override
	public void renderButton(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		super.renderButton(ms, mouseX, mouseY, partialTicks);

		if (hint == null || hint.isEmpty())
			return;

		if (!getValue().isEmpty())
			return;

		font.draw(ms, hint, x + 5, this.y + (this.height - 8) / 2, Theme.c(Theme.Key.TEXT).scaleAlpha(.75f).getRGB());
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
		InputMappings.Input mouseKey = InputMappings.getKey(code, p_keyPressed_2_);
		if (Minecraft.getInstance().options.keyInventory.isActiveAndMatches(mouseKey)) {
			return true;
		}

		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
	}
}
