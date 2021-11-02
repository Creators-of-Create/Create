package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSimiScreen extends Screen {

	protected int windowWidth, windowHeight;
	protected int windowXOffset, windowYOffset;
	protected int guiLeft, guiTop;
	protected List<AbstractWidget> widgets;

	protected AbstractSimiScreen(Component title) {
		super(title);
		widgets = new ArrayList<>();
	}

	protected AbstractSimiScreen() {
		this(new TextComponent(""));
	}

	protected void setWindowSize(int width, int height) {
		windowWidth = width;
		windowHeight = height;
	}

	protected void setWindowOffset(int xOffset, int yOffset) {
		windowXOffset = xOffset;
		windowYOffset = yOffset;
	}

	@Override
	protected void init() {
		super.init();
		guiLeft = (width - windowWidth) / 2;
		guiTop = (height - windowHeight) / 2;
		guiLeft += windowXOffset;
		guiTop += windowYOffset;
	}

	@Override
	public void tick() {
		super.tick();

		widgets.stream().filter(w -> w instanceof AbstractSimiWidget).forEach(w -> ((AbstractSimiWidget) w).tick());
	}

	@Override
	public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		partialTicks = partialTicks == 10 ? 0
				: Minecraft.getInstance()
				.getFrameTime();

		ms.pushPose();

		prepareFrame();

		renderWindowBackground(ms, mouseX, mouseY, partialTicks);
		renderWindow(ms, mouseX, mouseY, partialTicks);
		for (AbstractWidget widget : widgets)
			widget.render(ms, mouseX, mouseY, partialTicks);
		renderWindowForeground(ms, mouseX, mouseY, partialTicks);

		endFrame();

		ms.popPose();
	}

	protected void prepareFrame() {
	}

	protected void endFrame() {
	}

	protected void renderWindowBackground(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		renderBackground(ms);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean result = false;
		for (AbstractWidget widget : widgets)
			if (widget.mouseClicked(x, y, button))
				result = true;

		if (!result) {
			result = super.mouseClicked(x, y, button);
		}
		return result;
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		for (AbstractWidget widget : widgets)
			if (widget.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_))
				return true;

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

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected abstract void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks);

	protected void renderWindowForeground(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		for (AbstractWidget widget : widgets) {
			if (!widget.isHovered())
				continue;

			if (widget instanceof AbstractSimiWidget) {
				if (!((AbstractSimiWidget) widget).getToolTip().isEmpty())
					renderComponentTooltip(ms, ((AbstractSimiWidget) widget).getToolTip(), mouseX, mouseY);

			} else {
				widget.renderToolTip(ms, mouseX, mouseY);
			}
		}
	}

	@Deprecated
	protected void debugWindowArea(PoseStack matrixStack) {
		fill(matrixStack, guiLeft + windowWidth, guiTop + windowHeight, guiLeft, guiTop, 0xD3D3D3D3);
	}
	
	public List<AbstractWidget> getWidgets() {
		return widgets;
	}

}
