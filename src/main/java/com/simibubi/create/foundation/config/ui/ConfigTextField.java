package com.simibubi.create.foundation.config.ui;

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
	public void setFocus(boolean focus) {
		super.setFocus(focus);

		if (!focus) {
			if (ConfigScreenList.currentText == this)
				ConfigScreenList.currentText = null;

			return;
		}

		if (ConfigScreenList.currentText != null && ConfigScreenList.currentText != this)
			ConfigScreenList.currentText.setFocus(false);

		ConfigScreenList.currentText = this;
	}
}
