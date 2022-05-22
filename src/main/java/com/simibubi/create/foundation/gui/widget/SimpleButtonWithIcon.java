package com.simibubi.create.foundation.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;

public class SimpleButtonWithIcon extends Button {

	private final ResourceLocation icon;

	private final int iconW;
	private final int iconH;

	/**
	 * A simple button that renders the given icon over its center.
	 * @param icon the ResourceLocation pointing to the icon; requires full path, including 'textures/' and '.png'
	 * @param iconW the width the icon will display at, in pixels
	 * @param iconH the height the icon will display at, in pixels
	 */
	public SimpleButtonWithIcon(int x, int y, int width, int height, ResourceLocation icon, int iconW, int iconH, OnPress onPress) {
		super(x, y, width, height, TextComponent.EMPTY, onPress);
		this.icon = icon;
		this.iconW = iconW;
		this.iconH = iconH;
	}

	@Override
	public void renderButton(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			matrixStack.pushPose();
			super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
			RenderSystem.setShaderTexture(0, icon);
			int offsetX = (width / 2) - (iconW / 2);
			int offsetY = (height / 2) - (iconH / 2);
			blit(matrixStack, x + offsetX, y + offsetY, 0, 0, iconW, iconH, iconW, iconH);
			matrixStack.popPose();
		}
	}
}
