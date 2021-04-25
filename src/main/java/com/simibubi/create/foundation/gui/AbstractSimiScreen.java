package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractSimiScreen extends Screen {

	protected int sWidth, sHeight;
	protected int guiLeft, guiTop;
	protected List<Widget> widgets;

	protected AbstractSimiScreen() {
		super(new StringTextComponent(""));
		widgets = new ArrayList<>();
	}

	protected void setWindowSize(int width, int height) {
		sWidth = width;
		sHeight = height;
		guiLeft = (this.width - sWidth) / 2;
		guiTop = (this.height - sHeight) / 2;
	}

	@Override
	public void tick() {
		super.tick();

		widgets.stream().filter(w -> w instanceof AbstractSimiWidget).forEach(w -> ((AbstractSimiWidget) w).tick());
	}

	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		partialTicks = partialTicks == 10 ? 0
			: Minecraft.getInstance()
				.getRenderPartialTicks();

		ms.push();

		renderWindowBackground(ms, mouseX, mouseY, partialTicks);
		renderWindow(ms, mouseX, mouseY, partialTicks);
		for (Widget widget : widgets)
			widget.render(ms, mouseX, mouseY, partialTicks);
		renderWindowForeground(ms, mouseX, mouseY, partialTicks);

		ms.pop();
	}

	protected void renderWindowBackground(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		renderBackground(ms);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean result = false;
		for (Widget widget : widgets)
			if (widget.mouseClicked(x, y, button))
				result = true;

		if (!result) {
			result = super.mouseClicked(x, y, button);
		}
		return result;
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		for (Widget widget : widgets)
			if (widget.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_))
				return true;
		
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

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected abstract void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks);

	protected void renderWindowForeground(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		for (Widget widget : widgets) {
			if (!widget.isHovered())
				continue;

			if (widget instanceof AbstractSimiWidget) {
				if (!((AbstractSimiWidget) widget).getToolTip().isEmpty())
					renderTooltip(ms, ((AbstractSimiWidget) widget).getToolTip(), mouseX, mouseY);

			} else {
				widget.renderToolTip(ms, mouseX, mouseY);
			}
		}
	}

}
