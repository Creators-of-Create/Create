package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import com.simibubi.create.foundation.render.LegacyRenderSystemBridge;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;

public class RemovedGuiUtils {
	@NotNull
	private static ItemStack cachedTooltipStack = ItemStack.EMPTY;
	private static final int DEFAULT_BACKGROUND_COLOR = 0xf0100010;
	private static final int DEFAULT_BORDER_COLOR_START = 0x505000ff;
	private static final int DEFAULT_BORDER_COLOR_END = 0x5028007f;

	public static void preItemToolTip(@NotNull ItemStack stack) {
		cachedTooltipStack = stack;
	}

	public static void postItemToolTip() {
		cachedTooltipStack = ItemStack.EMPTY;
	}

	public static void drawHoveringText(GuiGraphicsExtractor graphics, List<? extends FormattedText> textLines, int mouseX,
		int mouseY, int screenWidth, int screenHeight, int maxTextWidth, Font font) {
		drawHoveringText(graphics, textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth,
			DEFAULT_BACKGROUND_COLOR, DEFAULT_BORDER_COLOR_START, DEFAULT_BORDER_COLOR_END,
			font);
	}

	public static void drawHoveringText(GuiGraphicsExtractor graphics, List<? extends FormattedText> textLines, int mouseX,
		int mouseY, int screenWidth, int screenHeight, int maxTextWidth, int backgroundColor, int borderColorStart,
		int borderColorEnd, Font font) {
		drawHoveringText(cachedTooltipStack, graphics, textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth,
			backgroundColor, borderColorStart, borderColorEnd, font);
	}

	public static void drawHoveringText(@NotNull final ItemStack stack, GuiGraphicsExtractor graphics,
		List<? extends FormattedText> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight,
		int maxTextWidth, Font font) {
		drawHoveringText(stack, graphics, textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth,
			DEFAULT_BACKGROUND_COLOR, DEFAULT_BORDER_COLOR_START, DEFAULT_BORDER_COLOR_END,
			font);
	}

	public static void drawHoveringText(@NotNull final ItemStack stack, GuiGraphicsExtractor graphics,
		List<? extends FormattedText> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight,
		int maxTextWidth, int backgroundColor, int borderColorStart, int borderColorEnd, Font font) {
		if (textLines.isEmpty())
			return;
		graphics.setComponentTooltipForNextFrame(font, textLines, mouseX, mouseY, stack);
	}
}
