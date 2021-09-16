package com.simibubi.create.content.schematics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllGuiTextures;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;

public class SchematicHotbarSlotOverlay extends GuiComponent {
	
	public void renderOn(PoseStack matrixStack, int slot) {
		Window mainWindow = Minecraft.getInstance().getWindow();
		int x = mainWindow.getGuiScaledWidth() / 2 - 88;
		int y = mainWindow.getGuiScaledHeight() - 19;
		RenderSystem.enableDepthTest();
		matrixStack.pushPose();
		AllGuiTextures.SCHEMATIC_SLOT.draw(matrixStack, this, x + 20 * slot, y);
		matrixStack.popPose();
	}

}
