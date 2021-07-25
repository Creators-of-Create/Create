package com.simibubi.create.foundation.config.ui;

import org.lwjgl.glfw.GLFW;

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

	@Override
	public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
		//prevent input of hex-keys from exiting the current config screen, in case they match the inventory key
		boolean ret = false;

		switch (p_231046_1_) {
			case GLFW.GLFW_KEY_A:
			case GLFW.GLFW_KEY_B:
			case GLFW.GLFW_KEY_C:
			case GLFW.GLFW_KEY_D:
			case GLFW.GLFW_KEY_E:
			case GLFW.GLFW_KEY_F:
				ret = true;
				break;
			default:
				break;
		}

		if (ret)
			return true;


		return super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
	}
}
