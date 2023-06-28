package com.simibubi.create.content.schematics.client;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class SchematicHotbarSlotOverlay  {
	
	public void renderOn(GuiGraphics graphics, int slot) {
		Window mainWindow = Minecraft.getInstance().getWindow();
		int x = mainWindow.getGuiScaledWidth() / 2 - 88;
		int y = mainWindow.getGuiScaledHeight() - 19;
		RenderSystem.enableDepthTest();
		AllGuiTextures.SCHEMATIC_SLOT.render(graphics, x + 20 * slot, y);
	}

}
