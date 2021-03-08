package com.simibubi.create.foundation.gui;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IScreenRenderable {

	@OnlyIn(Dist.CLIENT)
	void draw(AbstractGui screen, int x, int y);

	@OnlyIn(Dist.CLIENT)
	default void draw(int x, int y) {
		draw(new Screen(new StringTextComponent("")) {}, x, y);
	}
}
