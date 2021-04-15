package com.simibubi.create.foundation.config.ui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

public class ConfigTextField extends TextFieldWidget {

	protected FontRenderer font;
	protected String unit;

	public ConfigTextField(FontRenderer font, int x, int y, int width, int height, String unit) {
		super(font, x, y, width, height, StringTextComponent.EMPTY);
		this.font = font;
		this.unit = unit;
	}

	@Override
	public void renderButton(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		super.renderButton(ms, mouseX, mouseY, partialTicks);

		if (unit == null || unit.isEmpty())
			return;

		int unitWidth = font.getStringWidth(unit);
		if (this.font.getStringWidth(getText()) > (getAdjustedWidth() - unitWidth))
			return;

		font.draw(ms, unit, x + getAdjustedWidth() - unitWidth, this.y + (this.height - 8) / 2, 0xcc_aaaaaa);
	}
}
