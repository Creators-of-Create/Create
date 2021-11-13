package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
@ParametersAreNonnullByDefault
public abstract class AbstractSimiContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

	protected List<AbstractWidget> widgets;
	protected int windowXOffset;
	protected int windowYOffset;

	public AbstractSimiContainerScreen(T container, Inventory inv, Component title) {
		super(container, inv, title);
		widgets = new ArrayList<>();
	}

	protected void setWindowSize(int width, int height) {
		this.imageWidth = width;
		this.imageHeight = height;
	}

	protected void setWindowOffset(int xOffset, int yOffset) {
		windowXOffset = xOffset;
		windowYOffset = yOffset;
	}

	@Override
	protected void init() {
		super.init();
		leftPos += windowXOffset;
		topPos += windowYOffset;
	}

	@Override
	protected void renderLabels(PoseStack p_230451_1_, int p_230451_2_, int p_230451_3_) {
		// no-op to prevent screen- and inventory-title from being rendered at incorrect location
		// could also set this.titleX/Y and this.playerInventoryTitleX/Y to the proper values instead
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		partialTicks = Minecraft.getInstance()
			.getFrameTime();
		renderBackground(matrixStack);
		renderWindow(matrixStack, mouseX, mouseY, partialTicks);

		for (AbstractWidget widget : widgets)
			widget.render(matrixStack, mouseX, mouseY, partialTicks);

		super.render(matrixStack, mouseX, mouseY, partialTicks);

		renderWindowForeground(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean result = false;
		for (AbstractWidget widget : widgets) {
			if (widget.mouseClicked(x, y, button))
				result = true;
		}
		return result || super.mouseClicked(x, y, button);
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		for (AbstractWidget widget : widgets) {
			if (widget.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_))
				return true;
		}

		if (super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_))
			return true;

		InputConstants.Key mouseKey = InputConstants.getKey(code, p_keyPressed_2_);
		if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
			this.onClose();
			return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(char character, int code) {
		for (AbstractWidget widget : widgets) {
			if (widget.charTyped(character, code))
				return true;
		}
		return super.charTyped(character, code);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		for (AbstractWidget widget : widgets) {
			if (widget.mouseScrolled(mouseX, mouseY, delta))
				return true;
		}
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseReleased(double x, double y, int button) {
		boolean result = false;
		for (AbstractWidget widget : widgets) {
			if (widget.mouseReleased(x, y, button))
				result = true;
		}
		return result | super.mouseReleased(x, y, button);
	}

	protected abstract void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks);

	@Override
	protected void renderBg(PoseStack p_230450_1_, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
	}

	protected void renderWindowForeground(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderTooltip(matrixStack, mouseX, mouseY);
		for (AbstractWidget widget : widgets) {
			if (!widget.isHovered())
				continue;

			if (widget instanceof AbstractSimiWidget) {
				if (!((AbstractSimiWidget) widget).getToolTip().isEmpty())
					renderComponentTooltip(matrixStack, ((AbstractSimiWidget) widget).getToolTip(), mouseX, mouseY);

			} else {
				widget.renderToolTip(matrixStack, mouseX, mouseY);
			}
		}
	}

	public double getItemCountTextScale() {
		int guiScaleFactor = (int) minecraft.getWindow()
			.getGuiScale();
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

	public void renderPlayerInventory(PoseStack ms, int x, int y) {
		AllGuiTextures.PLAYER_INVENTORY.draw(ms, this, x, y);
		font.draw(ms, playerInventoryTitle, x + 8, y + 6, 0x404040);
	}

	/**
	 * Used for moving JEI out of the way of extra things like Flexcrate renders.
	 *
	 * <p>This screen class must be bound to a SlotMover instance for this method to work.
	 *
	 * @return the space that the gui takes up besides the normal rectangle defined by {@link ContainerScreen}.
	 */
	public List<Rect2i> getExtraAreas() {
		return Collections.emptyList();
	}

	@Deprecated
	protected void debugWindowArea(PoseStack matrixStack) {
		fill(matrixStack, leftPos + imageWidth, topPos + imageHeight, leftPos, topPos, 0xD3D3D3D3);
	}

	@Deprecated
	protected void debugExtraAreas(PoseStack matrixStack) {
		for (Rect2i area : getExtraAreas()) {
			fill(matrixStack, area.getX() + area.getWidth(), area.getY() + area.getHeight(), area.getX(), area.getY(), 0xd3d3d3d3);
		}
	}

}
