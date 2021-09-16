package com.simibubi.create.foundation.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.CreateClient;

import net.minecraft.client.gui.GuiComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IScreenRenderable {

	@OnlyIn(Dist.CLIENT)
	void draw(PoseStack ms, GuiComponent screen, int x, int y);

	@OnlyIn(Dist.CLIENT)
	default void draw(PoseStack ms, int x, int y) {
		draw(ms, CreateClient.EMPTY_SCREEN, x, y);
	}

}
