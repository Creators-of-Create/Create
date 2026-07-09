package com.simibubi.create.foundation.gui.menu;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import org.lwjgl.glfw.GLFW;

import com.simibubi.create.foundation.gui.AllGuiTextures;

import com.mojang.blaze3d.platform.InputConstants;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.client.gui.TickableGuiEventListener;
import net.createmod.catnip.api.client.gui.widget.AbstractSimiWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

import net.neoforged.api.distmarker.Dist;

@ParametersAreNonnullByDefault
public abstract class AbstractSimiContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

	protected int imageWidth, imageHeight;
	protected int windowXOffset, windowYOffset;

	public AbstractSimiContainerScreen(T container, Inventory inv, Component title) {
		super(container, inv, title);
		imageWidth = super.getImageWidth();
		imageHeight = super.getImageHeight();
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
		leftPos = (width - imageWidth) / 2 + windowXOffset;
		topPos = (height - imageHeight) / 2 + windowYOffset;
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

	/*@Override
	public void renderBackground(GuiGraphicsExtractor pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		NeoForge.EVENT_BUS.post(new ContainerScreenEvent.Render.Background(this, pGuiGraphics, pMouseX, pMouseY));
		renderBg(pGuiGraphics, pPartialTick, pMouseX, pMouseY);
	}*/

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		partialTicks = AnimationTickHolder.getGuiPartialTicks();

		renderBg(graphics, partialTicks, mouseX, mouseY);
		super.extractRenderState(graphics, mouseX, mouseY, partialTicks);

		renderForeground(graphics, mouseX, mouseY, partialTicks);
	}

	public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		extractRenderState(graphics, mouseX, mouseY, partialTicks);
	}

	protected void renderBg(GuiGraphicsExtractor graphics, float partialTicks, int mouseX, int mouseY) {
	}

	protected void renderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		extractTooltip(graphics, mouseX, mouseY);
	}

	protected int getSlotColor(int slotIndex) {
		return 0x80ffffff;
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		// no-op to prevent screen- and inventory-title from being rendered at incorrect
		// location
		// could also set this.titleX/Y and this.playerInventoryTitleX/Y to the proper
		// values instead
	}

	protected void renderForeground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		extractTooltip(graphics, mouseX, mouseY);
		for (Renderable widget : renderables) {
			if (widget instanceof AbstractSimiWidget simiWidget && simiWidget.isMouseOver(mouseX, mouseY)) {
				List<Component> tooltip = simiWidget.getToolTip();
				if (tooltip.isEmpty())
					continue;
				int ttx = simiWidget.lockedTooltipX == -1 ? mouseX : simiWidget.lockedTooltipX + simiWidget.getX();
				int tty = simiWidget.lockedTooltipY == -1 ? mouseY : simiWidget.lockedTooltipY + simiWidget.getY();
				graphics.setComponentTooltipForNextFrame(font, tooltip, ttx, tty);
			}
		}
	}

	public int getLeftOfCentered(int textureWidth) {
		return leftPos - windowXOffset + (imageWidth - textureWidth) / 2;
	}

	public void renderPlayerInventory(GuiGraphicsExtractor graphics, int x, int y) {
		AllGuiTextures.PLAYER_INVENTORY.render(graphics, x, y);
		graphics.text(font, playerInventoryTitle, x + 8, y + 6, 0x404040, false);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (getFocused() instanceof EditBox && event.key() != GLFW.GLFW_KEY_ESCAPE)
			return getFocused().keyPressed(event);
		if (keyPressed(event.key(), event.scancode(), event.modifiers()))
			return true;
		return super.keyPressed(event);
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return false;
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return charTyped((char) event.codepoint(), 0) || super.charTyped(event);
	}

	public boolean charTyped(char codePoint, int modifiers) {
		return false;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (getFocused() != null && !getFocused().isMouseOver(event.x(), event.y()))
			setFocused(null);
		if (mouseClicked(event.x(), event.y(), event.button()))
			return true;
		return super.mouseClicked(event, doubleClick);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	public boolean hasControlDown() {
		return InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)
			|| InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL);
	}

	public boolean hasShiftDown() {
		return InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
			|| InputConstants.isKeyDown(minecraft.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
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

	@Override
	public int getImageWidth() {
		return imageWidth;
	}

	@Override
	public int getImageHeight() {
		return imageHeight;
	}

	@Override
	public int getXSize() {
		return imageWidth;
	}

	@Override
	public int getYSize() {
		return imageHeight;
	}

	@Deprecated
	protected void debugWindowArea(GuiGraphicsExtractor graphics) {
		graphics.fill(leftPos + imageWidth, topPos + imageHeight, leftPos, topPos, 0xD3D3D3D3);
	}

	@Deprecated
	protected void debugExtraAreas(GuiGraphicsExtractor graphics) {
		for (Rect2i area : getExtraAreas()) {
			graphics.fill(area.getX() + area.getWidth(), area.getY() + area.getHeight(), area.getX(), area.getY(),
				0xD3D3D3D3);
		}
	}

	protected void playUiSound(SoundEvent sound, float volume, float pitch) {
		Minecraft.getInstance()
			.getSoundManager()
			.play(SimpleSoundInstance.forUI(sound, pitch, volume * 0.25f));
	}

}
