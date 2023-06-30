package com.simibubi.create.foundation.gui.menu;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.TickableGuiEventListener;
import com.simibubi.create.foundation.gui.widget.AbstractSimiWidget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public abstract class AbstractSimiContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

	protected int windowXOffset, windowYOffset;

	public AbstractSimiContainerScreen(T container, Inventory inv, Component title) {
		super(container, inv, title);
	}

	/**
	 * This method must be called before {@code super.init()}!
	 */
	protected void setWindowSize(int width, int height) {
		imageWidth = width;
		imageHeight = height;
	}

	/**
	 * This method must be called before {@code super.init()}!
	 */
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
	protected void containerTick() {
		for (GuiEventListener listener : children()) {
			if (listener instanceof TickableGuiEventListener tickable) {
				tickable.tick();
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected <W extends GuiEventListener & Renderable & NarratableEntry> void addRenderableWidgets(W... widgets) {
		for (W widget : widgets) {
			addRenderableWidget(widget);
		}
	}

	protected <W extends GuiEventListener & Renderable & NarratableEntry> void addRenderableWidgets(Collection<W> widgets) {
		for (W widget : widgets) {
			addRenderableWidget(widget);
		}
	}

	protected void removeWidgets(GuiEventListener... widgets) {
		for (GuiEventListener widget : widgets) {
			removeWidget(widget);
		}
	}

	protected void removeWidgets(Collection<? extends GuiEventListener> widgets) {
		for (GuiEventListener widget : widgets) {
			removeWidget(widget);
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		partialTicks = minecraft.getFrameTime();

		renderBackground(graphics);

		super.render(graphics, mouseX, mouseY, partialTicks);

		renderForeground(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		// no-op to prevent screen- and inventory-title from being rendered at incorrect
		// location
		// could also set this.titleX/Y and this.playerInventoryTitleX/Y to the proper
		// values instead
	}

	protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		renderTooltip(graphics, mouseX, mouseY);
		for (Renderable widget : renderables) {
			if (widget instanceof AbstractSimiWidget simiWidget && simiWidget.isMouseOver(mouseX, mouseY)) {
				List<Component> tooltip = simiWidget.getToolTip();
				if (tooltip.isEmpty())
					continue;
				int ttx = simiWidget.lockedTooltipX == -1 ? mouseX : simiWidget.lockedTooltipX + simiWidget.getX();
				int tty = simiWidget.lockedTooltipY == -1 ? mouseY : simiWidget.lockedTooltipY + simiWidget.getY();
				graphics.renderComponentTooltip(font, tooltip, ttx, tty);
			}
		}
	}

	public int getLeftOfCentered(int textureWidth) {
		return leftPos - windowXOffset + (imageWidth - textureWidth) / 2;
	}

	public void renderPlayerInventory(GuiGraphics graphics, int x, int y) {
		AllGuiTextures.PLAYER_INVENTORY.render(graphics, x, y);
		graphics.drawString(font, playerInventoryTitle, x + 8, y + 6, 0x404040, false);
	}

	@Override
	public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
		InputConstants.Key mouseKey = InputConstants.getKey(pKeyCode, pScanCode);
		if (getFocused() instanceof EditBox && this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey))
			return false;
		return super.keyPressed(pKeyCode, pScanCode, pModifiers);
	}
	
	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if (getFocused() != null && !getFocused().isMouseOver(pMouseX, pMouseY))
			setFocused(null);
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}
	
	@Override
	public GuiEventListener getFocused() {
		GuiEventListener focused = super.getFocused();
		if (focused instanceof AbstractWidget && !((AbstractWidget) focused).isFocused())
			focused = null;
		setFocused(focused);
		return focused;
	}

	/**
	 * Used for moving JEI out of the way of extra things like block renders.
	 *
	 * @return the space that the GUI takes up outside the normal rectangle defined
	 *         by {@link ContainerScreen}.
	 */
	public List<Rect2i> getExtraAreas() {
		return Collections.emptyList();
	}

	@Deprecated
	protected void debugWindowArea(GuiGraphics graphics) {
		graphics.fill(leftPos + imageWidth, topPos + imageHeight, leftPos, topPos, 0xD3D3D3D3);
	}

	@Deprecated
	protected void debugExtraAreas(GuiGraphics graphics) {
		for (Rect2i area : getExtraAreas()) {
			graphics.fill(area.getX() + area.getWidth(), area.getY() + area.getHeight(), area.getX(), area.getY(),
				0xD3D3D3D3);
		}
	}

}
