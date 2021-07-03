package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public abstract class AbstractSimiContainerScreen<T extends Container> extends ContainerScreen<T> {

	protected List<Widget> widgets;
	protected int windowXOffset;
	protected int windowYOffset;

	public AbstractSimiContainerScreen(T container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title);
		widgets = new ArrayList<>();
	}

	protected void setWindowSize(int width, int height) {
		this.xSize = width;
		this.ySize = height;
	}

	protected void setWindowOffset(int xOffset, int yOffset) {
		windowXOffset = xOffset;
		windowYOffset = yOffset;
	}

	@Override
	protected void init() {
		super.init();
		guiLeft += windowXOffset;
		guiTop += windowYOffset;
	}

	@Override
	protected void drawForeground(MatrixStack p_230451_1_, int p_230451_2_, int p_230451_3_) {
		// no-op to prevent screen- and inventory-title from being rendered at incorrect location
		// could also set this.titleX/Y and this.playerInventoryTitleX/Y to the proper values instead
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		partialTicks = Minecraft.getInstance()
			.getRenderPartialTicks();
		renderBackground(matrixStack);
		renderWindow(matrixStack, mouseX, mouseY, partialTicks);

		for (Widget widget : widgets)
			widget.render(matrixStack, mouseX, mouseY, partialTicks);

		super.render(matrixStack, mouseX, mouseY, partialTicks);

		RenderSystem.enableAlphaTest();
		RenderSystem.enableBlend();
		RenderSystem.disableRescaleNormal();
		RenderHelper.disableStandardItemLighting();
		RenderSystem.disableLighting();
		RenderSystem.disableDepthTest();
		renderWindowForeground(matrixStack, mouseX, mouseY, partialTicks);
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

		if (super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_))
			return true;

		InputMappings.Input mouseKey = InputMappings.getInputByCode(code, p_keyPressed_2_);
		if (this.client.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey)) {
			this.onClose();
			return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(char character, int code) {
		for (Widget widget : widgets) {
			if (widget.charTyped(character, code))
				return true;
		}
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

	protected abstract void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks);

	@Override
	protected void drawBackground(MatrixStack p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
	}

	protected void renderWindowForeground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		drawMouseoverTooltip(matrixStack, mouseX, mouseY);
		for (Widget widget : widgets) {
			if (!widget.isHovered())
				continue;

			if (widget instanceof AbstractSimiWidget) {
				if (!((AbstractSimiWidget) widget).getToolTip().isEmpty())
					renderTooltip(matrixStack, ((AbstractSimiWidget) widget).getToolTip(), mouseX, mouseY);

			} else {
				widget.renderToolTip(matrixStack, mouseX, mouseY);
			}
		}
	}

	public double getItemCountTextScale() {
		int guiScaleFactor = (int) client.getWindow()
			.getGuiScaleFactor();
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

	public int getLeftOfCentered(int textureWidth) {
		return (width - textureWidth) / 2;
	}

	public void renderPlayerInventory(MatrixStack ms, int x, int y) {
		AllGuiTextures.PLAYER_INVENTORY.draw(ms, this, x, y);
		textRenderer.draw(ms, playerInventory.getDisplayName(), x + 8, y + 6, 0x404040);
	}

	/**
	 * Used for moving JEI out of the way of extra things like Flexcrate renders.
	 *
	 * <p>This screen class must be bound to a SlotMover instance for this method to work.
	 *
	 * @return the space that the gui takes up besides the normal rectangle defined by {@link ContainerScreen}.
	 */
	public List<Rectangle2d> getExtraAreas() {
		return Collections.emptyList();
	}

	// Not up to date with ItemRenderer
	@Deprecated
	protected void renderItemOverlayIntoGUI(MatrixStack matrixStack, FontRenderer fr, ItemStack stack, int xPosition,
		int yPosition, @Nullable String text, int textColor) {
		if (!stack.isEmpty()) {
			if (stack.getItem()
				.showDurabilityBar(stack)) {
				RenderSystem.disableLighting();
				RenderSystem.disableDepthTest();
				RenderSystem.disableTexture();
				RenderSystem.disableAlphaTest();
				RenderSystem.disableBlend();
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();
				double health = stack.getItem()
					.getDurabilityForDisplay(stack);
				int i = Math.round(13.0F - (float) health * 13.0F);
				int j = stack.getItem()
					.getRGBDurabilityForDisplay(stack);
				this.draw(bufferbuilder, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
				this.draw(bufferbuilder, xPosition + 2, yPosition + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255,
					255);
				RenderSystem.enableBlend();
				RenderSystem.enableAlphaTest();
				RenderSystem.enableTexture();
				RenderSystem.enableLighting();
				RenderSystem.enableDepthTest();
			}

			if (stack.getCount() != 1 || text != null) {
				String s = text == null ? String.valueOf(stack.getCount()) : text;
				RenderSystem.disableLighting();
				RenderSystem.disableDepthTest();
				RenderSystem.disableBlend();
				matrixStack.push();

				int guiScaleFactor = (int) client.getWindow()
					.getGuiScaleFactor();
				matrixStack.translate(xPosition + 16.5f, yPosition + 16.5f, 0);
				double scale = getItemCountTextScale();

				matrixStack.scale((float) scale, (float) scale, 0);
				matrixStack.translate(-fr.getStringWidth(s) - (guiScaleFactor > 1 ? 0 : -.5f),
					-textRenderer.FONT_HEIGHT + (guiScaleFactor > 1 ? 1 : 1.75f), 0);
				fr.drawWithShadow(matrixStack, s, 0, 0, textColor);

				matrixStack.pop();
				RenderSystem.enableBlend();
				RenderSystem.enableLighting();
				RenderSystem.enableDepthTest();
				RenderSystem.enableBlend();
			}
		}
	}

	@Deprecated
	private void draw(BufferBuilder renderer, int x, int y, int width, int height, int red, int green, int blue,
		int alpha) {
		renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		renderer.vertex((double) (x + 0), (double) (y + 0), 0.0D)
			.color(red, green, blue, alpha)
			.endVertex();
		renderer.vertex((double) (x + 0), (double) (y + height), 0.0D)
			.color(red, green, blue, alpha)
			.endVertex();
		renderer.vertex((double) (x + width), (double) (y + height), 0.0D)
			.color(red, green, blue, alpha)
			.endVertex();
		renderer.vertex((double) (x + width), (double) (y + 0), 0.0D)
			.color(red, green, blue, alpha)
			.endVertex();
		Tessellator.getInstance()
			.draw();
	}

	@Deprecated
	protected void debugWindowArea(MatrixStack matrixStack) {
		fill(matrixStack, guiLeft + xSize, guiTop + ySize, guiLeft, guiTop, 0xD3D3D3D3);
	}

	@Deprecated
	protected void debugExtraAreas(MatrixStack matrixStack) {
		for (Rectangle2d area : getExtraAreas()) {
			fill(matrixStack, area.getX() + area.getWidth(), area.getY() + area.getHeight(), area.getX(), area.getY(), 0xd3d3d3d3);
		}
	}

}
