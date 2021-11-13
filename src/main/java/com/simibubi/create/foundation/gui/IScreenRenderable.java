package com.simibubi.create.foundation.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.CreateClient;

import net.minecraft.client.gui.GuiComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public interface IScreenRenderable {

	@Environment(EnvType.CLIENT)
	void draw(PoseStack ms, GuiComponent screen, int x, int y);

	@Environment(EnvType.CLIENT)
	default void draw(PoseStack ms, int x, int y) {
		draw(ms, CreateClient.EMPTY_SCREEN, x, y);
	}

}
