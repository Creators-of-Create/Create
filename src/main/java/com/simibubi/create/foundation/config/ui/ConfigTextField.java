package com.simibubi.create.foundation.config.ui;

import net.minecraft.client.gui.FontRenderer;

public class ConfigTextField extends HintableTextFieldWidget {

	public ConfigTextField(FontRenderer font, int x, int y, int width, int height) {
		super(font, x, y, width, height);
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
