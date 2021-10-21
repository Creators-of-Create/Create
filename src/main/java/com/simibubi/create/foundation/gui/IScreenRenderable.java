package com.simibubi.create.foundation.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.CreateClient;

import net.minecraft.client.gui.AbstractGui;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IScreenRenderable {

	@OnlyIn(Dist.CLIENT)
	void draw(MatrixStack ms, AbstractGui screen, int x, int y);

	@OnlyIn(Dist.CLIENT)
	default void draw(MatrixStack ms, int x, int y) {
		draw(ms, CreateClient.EMPTY_SCREEN, x, y);
	}

}
