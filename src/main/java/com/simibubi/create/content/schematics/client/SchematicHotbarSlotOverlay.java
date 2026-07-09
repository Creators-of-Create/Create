package com.simibubi.create.content.schematics.client;

import com.mojang.blaze3d.platform.Window;
import com.simibubi.create.foundation.render.LegacyRenderSystemBridge;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class SchematicHotbarSlotOverlay  {

	public void renderOn(GuiGraphicsExtractor graphics, int slot) {
		Window mainWindow = Minecraft.getInstance().getWindow();
		int x = mainWindow.getGuiScaledWidth() / 2 - 88;
		int y = mainWindow.getGuiScaledHeight() - 19;
		LegacyRenderSystemBridge.enableDepthTest();
		AllGuiTextures.SCHEMATIC_SLOT.render(graphics, x + 20 * slot, y);
	}

}
