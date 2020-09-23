package com.simibubi.create.content.schematics.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public class SchematicHotbarSlotOverlay extends AbstractGui {
	
	public void renderOn(MatrixStack matrixStack, int slot) {
		MainWindow mainWindow = Minecraft.getInstance().getWindow();
		int x = mainWindow.getScaledWidth() / 2 - 92;
		int y = mainWindow.getScaledHeight() - 23;
		RenderSystem.enableAlphaTest();
		AllGuiTextures.BLUEPRINT_SLOT.draw(matrixStack, this, x + 20 * slot, y);
		RenderSystem.disableAlphaTest();
	}

}
