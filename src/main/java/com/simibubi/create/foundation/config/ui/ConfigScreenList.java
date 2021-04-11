package com.simibubi.create.foundation.config.ui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.StencilElement;
import com.simibubi.create.foundation.gui.TextStencilElement;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.IFormattableTextComponent;

public class ConfigScreenList extends ExtendedList<ConfigScreenList.Entry> {


	public ConfigScreenList(Minecraft client, int width, int height, int top, int bottom, int elementHeight) {
		super(client, width, height, top, bottom, elementHeight);
		func_244605_b(false);
		func_244606_c(false);
	}

	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		//render tmp background
		fill(ms, left, top, left+width, top+height, 0x44_000000);

		super.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	public int getRowWidth() {
		return width-18;
	}

	@Override
	protected int getScrollbarPositionX() {
		return left + this.width-5;
	}

	public void tick() {
		children().forEach(Entry::tick);
	}

	public static abstract class Entry extends ExtendedList.AbstractListEntry<Entry> {
		public void tick() {}
	}

	public static class LabeledEntry extends Entry {
		protected TextStencilElement label;

		public LabeledEntry(String label) {
			this.label = new TextStencilElement(Minecraft.getInstance().fontRenderer, label);
		}

		@Override
		public void render(MatrixStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
			UIRenderHelper.streak(ms, 0, x, y+height/2, height, width/2, 0x0);
			IFormattableTextComponent component = label.getComponent();
			if (Minecraft.getInstance().fontRenderer.getWidth(component) > width/2 - 10) {
				label.withText(Minecraft.getInstance().fontRenderer.trimToWidth(component, width / 2 - 15).getString() + "...");
			}
			label.at(x + 5, y + height/2 - 4, 0).render(ms);
		}
	}

	public static class WrappedEntry extends Entry {

		AbstractSimiWidget widget;

		public WrappedEntry(AbstractSimiWidget widget) {
			this.widget = widget;
		}

		@Override
		public void tick() {
			if (widget instanceof ConfigButton)
				((ConfigButton) widget).tick();
		}

		@Override
		public void render(MatrixStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
			widget.x = x;
			widget.y = y;
			widget.setWidth(width);
			widget.setHeight(height);
			widget.render(ms, mouseX, mouseY, partialTicks);
		}

		@Override
		public boolean mouseClicked(double x, double y, int button) {
			return widget.mouseClicked(x, y, button);
		}
	}
}
