package com.simibubi.create.gui;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public class BlueprintHotbarOverlay extends AbstractGui {
	
	public void renderOn(int slot) {
		MainWindow mainWindow = Minecraft.getInstance().mainWindow;
		int x = mainWindow.getScaledWidth() / 2 - 92;
		int y = mainWindow.getScaledHeight() - 23;
		GlStateManager.enableAlphaTest();
		GuiResources.BLUEPRINT_SLOT.draw(this, x + 20 * slot, y);
		GlStateManager.disableAlphaTest();
	}

}
