package com.simibubi.create.foundation.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IScreenRenderable {

	@OnlyIn(Dist.CLIENT)
	Screen EMPTY_SCREEN = new Screen(new StringTextComponent("")) {};

	@OnlyIn(Dist.CLIENT)
	void draw(MatrixStack ms, AbstractGui screen, int x, int y);

	@OnlyIn(Dist.CLIENT)
	default void draw(MatrixStack ms, int x, int y) {
		draw(ms, EMPTY_SCREEN, x, y);
	}

}
