package com.simibubi.create.compat.jei.category;

import com.simibubi.create.foundation.gui.AllGuiTextures;

import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;

public class WidgetUtil {
	public static Widget textured(AllGuiTextures texture, int x, int y) {
		return Widgets.createTexturedWidget(texture.location, x, y, texture.width, texture.height);
	}
}
