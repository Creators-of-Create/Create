package com.simibubi.create.modules.schematics.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.ScreenResources;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public class BlueprintHotbarOverlay extends AbstractGui {
	
	public void renderOn(int slot) {
		MainWindow mainWindow = Minecraft.getInstance().getWindow();
		int x = mainWindow.getScaledWidth() / 2 - 92;
		int y = mainWindow.getScaledHeight() - 23;
		RenderSystem.enableAlphaTest();
		ScreenResources.BLUEPRINT_SLOT.draw(this, x + 20 * slot, y);
		RenderSystem.disableAlphaTest();
	}

}
