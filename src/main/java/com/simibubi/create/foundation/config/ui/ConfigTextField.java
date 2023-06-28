package com.simibubi.create.foundation.config.ui;

import net.minecraft.client.gui.Font;

public class ConfigTextField extends HintableTextFieldWidget {

	public ConfigTextField(Font font, int x, int y, int width, int height) {
		super(font, x, y, width, height);
	}

	@Override
	public void setFocused(boolean focus) {
		super.setFocused(focus);

		if (!focus) {
			if (ConfigScreenList.currentText == this)
				ConfigScreenList.currentText = null;

			return;
		}

		if (ConfigScreenList.currentText != null && ConfigScreenList.currentText != this)
			ConfigScreenList.currentText.setFocused(false);

		ConfigScreenList.currentText = this;
	}
}
