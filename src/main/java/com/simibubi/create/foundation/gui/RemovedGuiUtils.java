package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.extensions.IForgeGuiGraphics;
import net.minecraftforge.common.MinecraftForge;

public class RemovedGuiUtils {
	@Nonnull
	private static ItemStack cachedTooltipStack = ItemStack.EMPTY;

	public static void preItemToolTip(@Nonnull ItemStack stack) {
		cachedTooltipStack = stack;
	}

	public static void postItemToolTip() {
		cachedTooltipStack = ItemStack.EMPTY;
	}

	public static void drawHoveringText(GuiGraphics graphics, List<? extends FormattedText> textLines, int mouseX,
		int mouseY, int screenWidth, int screenHeight, int maxTextWidth, Font font) {
		drawHoveringText(graphics, textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth,
			IForgeGuiGraphics.DEFAULT_BACKGROUND_COLOR, IForgeGuiGraphics.DEFAULT_BORDER_COLOR_START, IForgeGuiGraphics.DEFAULT_BORDER_COLOR_END,
			font);
	}

	public static void drawHoveringText(GuiGraphics graphics, List<? extends FormattedText> textLines, int mouseX,
		int mouseY, int screenWidth, int screenHeight, int maxTextWidth, int backgroundColor, int borderColorStart,
		int borderColorEnd, Font font) {
		drawHoveringText(cachedTooltipStack, graphics, textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth,
			backgroundColor, borderColorStart, borderColorEnd, font);
	}

	public static void drawHoveringText(@Nonnull final ItemStack stack, GuiGraphics graphics,
		List<? extends FormattedText> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight,
		int maxTextWidth, Font font) {
		drawHoveringText(stack, graphics, textLines, mouseX, mouseY, screenWidth, screenHeight, maxTextWidth,
			IForgeGuiGraphics.DEFAULT_BACKGROUND_COLOR, IForgeGuiGraphics.DEFAULT_BORDER_COLOR_START, IForgeGuiGraphics.DEFAULT_BORDER_COLOR_END,
			font);
	}

	public static void drawHoveringText(@Nonnull final ItemStack stack, GuiGraphics graphics,
		List<? extends FormattedText> textLines, int mouseX, int mouseY, int screenWidth, int screenHeight,
		int maxTextWidth, int backgroundColor, int borderColorStart, int borderColorEnd, Font font) {
		if (textLines.isEmpty())
			return;

		List<ClientTooltipComponent> list = ForgeHooksClient.gatherTooltipComponents(stack, textLines,
			stack.getTooltipImage(), mouseX, screenWidth, screenHeight, font);
		RenderTooltipEvent.Pre event =
			new RenderTooltipEvent.Pre(stack, graphics, mouseX, mouseY, screenWidth, screenHeight, font, list, null);
		if (MinecraftForge.EVENT_BUS.post(event))
			return;

		PoseStack pStack = graphics.pose();
		
		mouseX = event.getX();
		mouseY = event.getY();
		screenWidth = event.getScreenWidth();
		screenHeight = event.getScreenHeight();
		font = event.getFont();

		// RenderSystem.disableRescaleNormal();
		RenderSystem.disableDepthTest();
		int tooltipTextWidth = 0;

		for (FormattedText textLine : textLines) {
			int textLineWidth = font.width(textLine);
			if (textLineWidth > tooltipTextWidth)
				tooltipTextWidth = textLineWidth;
		}

		boolean needsWrap = false;

		int titleLinesCount = 1;
		int tooltipX = mouseX + 12;
		if (tooltipX + tooltipTextWidth + 4 > screenWidth) {
			tooltipX = mouseX - 16 - tooltipTextWidth;
			if (tooltipX < 4) // if the tooltip doesn't fit on the screen
			{
				if (mouseX > screenWidth / 2)
					tooltipTextWidth = mouseX - 12 - 8;
				else
					tooltipTextWidth = screenWidth - 16 - mouseX;
				needsWrap = true;
			}
		}

		if (maxTextWidth > 0 && tooltipTextWidth > maxTextWidth) {
			tooltipTextWidth = maxTextWidth;
			needsWrap = true;
		}

		if (needsWrap) {
			int wrappedTooltipWidth = 0;
			List<FormattedText> wrappedTextLines = new ArrayList<>();
			for (int i = 0; i < textLines.size(); i++) {
				FormattedText textLine = textLines.get(i);
				List<FormattedText> wrappedLine = font.getSplitter()
					.splitLines(textLine, tooltipTextWidth, Style.EMPTY);
				if (i == 0)
					titleLinesCount = wrappedLine.size();

				for (FormattedText line : wrappedLine) {
					int lineWidth = font.width(line);
					if (lineWidth > wrappedTooltipWidth)
						wrappedTooltipWidth = lineWidth;
					wrappedTextLines.add(line);
				}
			}
			tooltipTextWidth = wrappedTooltipWidth;
			textLines = wrappedTextLines;

			if (mouseX > screenWidth / 2)
				tooltipX = mouseX - 16 - tooltipTextWidth;
			else
				tooltipX = mouseX + 12;
		}

		int tooltipY = mouseY - 12;
		int tooltipHeight = 8;

		if (textLines.size() > 1) {
			tooltipHeight += (textLines.size() - 1) * 10;
			if (textLines.size() > titleLinesCount)
				tooltipHeight += 2; // gap between title lines and next lines
		}

		if (tooltipY < 4)
			tooltipY = 4;
		else if (tooltipY + tooltipHeight + 4 > screenHeight)
			tooltipY = screenHeight - tooltipHeight - 4;

		final int zLevel = 400;
		RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(stack, graphics, tooltipX, tooltipY,
			font, backgroundColor, borderColorStart, borderColorEnd, list);
		MinecraftForge.EVENT_BUS.post(colorEvent);
		backgroundColor = colorEvent.getBackgroundStart();
		borderColorStart = colorEvent.getBorderStart();
		borderColorEnd = colorEvent.getBorderEnd();

		pStack.pushPose();
		Matrix4f mat = pStack.last()
			.pose();
		graphics.fillGradient(tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3,
			tooltipY - 3, zLevel, backgroundColor, backgroundColor);
		graphics.fillGradient(tooltipX - 3, tooltipY + tooltipHeight + 3,
			tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 4, zLevel, backgroundColor, backgroundColor);
		graphics.fillGradient(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3,
			tooltipY + tooltipHeight + 3, zLevel, backgroundColor, backgroundColor);
		graphics.fillGradient(tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3,
			zLevel, backgroundColor, backgroundColor);
		graphics.fillGradient(tooltipX + tooltipTextWidth + 3, tooltipY - 3,
			tooltipX + tooltipTextWidth + 4, tooltipY + tooltipHeight + 3, zLevel, backgroundColor, backgroundColor);
		graphics.fillGradient(tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1,
			tooltipY + tooltipHeight + 3 - 1, zLevel, borderColorStart, borderColorEnd);
		graphics.fillGradient(tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1,
			tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3 - 1, zLevel, borderColorStart, borderColorEnd);
		graphics.fillGradient(tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3,
			tooltipY - 3 + 1, zLevel, borderColorStart, borderColorStart);
		graphics.fillGradient(tooltipX - 3, tooltipY + tooltipHeight + 2,
			tooltipX + tooltipTextWidth + 3, tooltipY + tooltipHeight + 3, zLevel, borderColorEnd, borderColorEnd);

		MultiBufferSource.BufferSource renderType = MultiBufferSource.immediate(Tesselator.getInstance()
			.getBuilder());
		pStack.translate(0.0D, 0.0D, zLevel);

		for (int lineNumber = 0; lineNumber < list.size(); ++lineNumber) {
			ClientTooltipComponent line = list.get(lineNumber);

			if (line != null)
				line.renderText(font, tooltipX, tooltipY, mat, renderType);

			if (lineNumber + 1 == titleLinesCount)
				tooltipY += 2;

			tooltipY += line == null ? 10 : line.getHeight();
		}

		renderType.endBatch();
		pStack.popPose();

		RenderSystem.enableDepthTest();
	}
}
