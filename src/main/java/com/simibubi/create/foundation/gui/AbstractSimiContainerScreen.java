package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
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

	/**
	 * Used for moving JEI out of the way of extra things like Flexcrate renders
	 *
	 * @return the space that the gui takes up besides the normal rectangle defined by {@link ContainerScreen}.
	 */
	public List<Rectangle2d> getExtraAreas() {
		return Collections.emptyList();
	}
}
