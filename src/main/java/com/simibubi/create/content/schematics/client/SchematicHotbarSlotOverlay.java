package com.simibubi.create.content.schematics.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;

public class SchematicHotbarSlotOverlay extends AbstractGui {
	
	public void renderOn(int slot) {
		MainWindow mainWindow = Minecraft.getInstance().getWindow();
		int x = mainWindow.getScaledWidth() / 2 - 88;
		int y = mainWindow.getScaledHeight() - 19;
		RenderSystem.enableAlphaTest();
		AllGuiTextures.SCHEMATIC_SLOT.draw(this, x + 20 * slot, y);
		RenderSystem.disableAlphaTest();
	}

}
