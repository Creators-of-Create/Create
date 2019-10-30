package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSimiContainerScreen<T extends Container> extends ContainerScreen<T> {

	protected List<Widget> widgets;

	protected AbstractSimiContainerScreen(T container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
		widgets = new ArrayList<>();
	}

	protected void setWindowSize(int width, int height) {
		this.xSize = width;
		this.ySize = height;
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		renderBackground();
		renderWindow(mouseX, mouseY, partialTicks);
		
		super.render(mouseX, mouseY, partialTicks);
		
		GlStateManager.enableAlphaTest();
		GlStateManager.enableBlend();
		GlStateManager.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.disableDepthTest();
		
		for (Widget widget : widgets)
			widget.render(mouseX, mouseY, partialTicks);
		renderWindowForeground(mouseX, mouseY, partialTicks);
		for (Widget widget : widgets)
			widget.renderToolTip(mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean result = false;
		for (Widget widget : widgets) {
			if (widget.mouseClicked(x, y, button))
				result = true;
		}
		return result || super.mouseClicked(x, y, button);
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		for (Widget widget : widgets) {
			if (widget.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_))
				return true;
		}
		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
	}

	@Override
	public boolean charTyped(char character, int code) {
		for (Widget widget : widgets) {
			if (widget.charTyped(character, code))
				return true;
		}
		if (character == 'e')
			onClose();
		return super.charTyped(character, code);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		for (Widget widget : widgets) {
			if (widget.mouseScrolled(mouseX, mouseY, delta))
				return true;
		}
		return super.mouseScrolled(mouseX, mouseY, delta);
	}
	
	@Override
	public boolean mouseReleased(double x, double y, int button) {
		boolean result = false;
		for (Widget widget : widgets) {
			if (widget.mouseReleased(x, y, button))
				result = true;
		}
		return result | super.mouseReleased(x, y, button);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected abstract void renderWindow(int mouseX, int mouseY, float partialTicks);

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
	}

	protected void renderWindowForeground(int mouseX, int mouseY, float partialTicks) {
		super.renderHoveredToolTip(mouseX, mouseY);
		for (Widget widget : widgets) {
			if (!widget.isHovered())
				continue;

			if (widget instanceof AbstractSimiWidget && !((AbstractSimiWidget) widget).getToolTip().isEmpty()) {
				renderTooltip(((AbstractSimiWidget) widget).getToolTip(), mouseX, mouseY);
			}
		}
	}
	
	protected void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition,
			@Nullable String text, int textColor) {
		if (!stack.isEmpty()) {
			if (stack.getItem().showDurabilityBar(stack)) {
				GlStateManager.disableLighting();
				GlStateManager.disableDepthTest();
				GlStateManager.disableTexture();
				GlStateManager.disableAlphaTest();
				GlStateManager.disableBlend();
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();
				double health = stack.getItem().getDurabilityForDisplay(stack);
				int i = Math.round(13.0F - (float) health * 13.0F);
				int j = stack.getItem().getRGBDurabilityForDisplay(stack);
				this.draw(bufferbuilder, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
				this.draw(bufferbuilder, xPosition + 2, yPosition + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255,
						255);
				GlStateManager.enableBlend();
				GlStateManager.enableAlphaTest();
				GlStateManager.enableTexture();
				GlStateManager.enableLighting();
				GlStateManager.enableDepthTest();
			}

			if (stack.getCount() != 1 || text != null) {
				String s = text == null ? String.valueOf(stack.getCount()) : text;
				GlStateManager.disableLighting();
				GlStateManager.disableDepthTest();
				GlStateManager.disableBlend();
				GlStateManager.pushMatrix();

				int guiScaleFactor = (int) minecraft.mainWindow.getGuiScaleFactor();
				GlStateManager.translated((float) (xPosition + 16.5f), (float) (yPosition + 16.5f), 0);
				double scale = getItemCountTextScale();

				GlStateManager.scaled(scale, scale, 0);
				GlStateManager.translated(-fr.getStringWidth(s) - (guiScaleFactor > 1 ? 0 : -.5f),
						-font.FONT_HEIGHT + (guiScaleFactor > 1 ? 1 : 1.75f), 0);
				fr.drawStringWithShadow(s, 0, 0, textColor);

				GlStateManager.popMatrix();
				GlStateManager.enableBlend();
				GlStateManager.enableLighting();
				GlStateManager.enableDepthTest();
				GlStateManager.enableBlend();
			}
		}
	}

	public double getItemCountTextScale() {
		int guiScaleFactor = (int) minecraft.mainWindow.getGuiScaleFactor();
		double scale = 1;
		switch (guiScaleFactor) {
		case 1:
			scale = 2060 / 2048d;
			break;
		case 2:
			scale = .5;
			break;
		case 3:
			scale = .675;
			break;
		case 4:
			scale = .75;
			break;
		default:
			scale = ((float) guiScaleFactor - 1) / guiScaleFactor;
		}
		return scale;
	}

	private void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue,
			int alpha) {
		renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
		renderer.pos((double) (x + 0), (double) (y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.pos((double) (x + 0), (double) (y + height), 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.pos((double) (x + width), (double) (y + height), 0.0D).color(red, green, blue, alpha).endVertex();
		renderer.pos((double) (x + width), (double) (y + 0), 0.0D).color(red, green, blue, alpha).endVertex();
		Tessellator.getInstance().draw();
	}

	/**
	 * Used for moving JEI out of the way of extra things like Flexcrate renders
	 *
	 * @return the space that the gui takes up besides the normal rectangle defined by {@link ContainerScreen}.
	 */
	public List<Rectangle2d> getExtraAreas() {
		return Collections.emptyList();
	}
}
