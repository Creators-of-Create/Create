package com.simibubi.create.modules.schematics.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.ScreenResources;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public class SchematicHotbarSlotOverlay extends AbstractGui {
	
	public void renderOn(int slot) {
		MainWindow mainWindow = Minecraft.getInstance().mainWindow;
		int x = mainWindow.getScaledWidth() / 2 - 92;
		int y = mainWindow.getScaledHeight() - 23;
		GlStateManager.enableAlphaTest();
		ScreenResources.BLUEPRINT_SLOT.draw(this, x + 20 * slot, y);
		GlStateManager.disableAlphaTest();
	}

}
