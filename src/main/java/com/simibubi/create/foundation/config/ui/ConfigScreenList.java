package com.simibubi.create.foundation.config.ui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.TextStencilElement;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.IFormattableTextComponent;

public class ConfigScreenList extends ExtendedList<ConfigScreenList.Entry> {

	public TextFieldWidget currentText;

	public boolean isForServer = false;

	public ConfigScreenList(Minecraft client, int width, int height, int top, int bottom, int elementHeight) {
		super(client, width, height, top, bottom, elementHeight);
		func_244605_b(false);
		func_244606_c(false);
		setRenderSelection(false);
	}

	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		//render tmp background
		fill(ms, left, top, left+width, top+height, 0x44_000000);

		super.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderList(MatrixStack p_238478_1_, int p_238478_2_, int p_238478_3_, int p_238478_4_, int p_238478_5_, float p_238478_6_) {
		MainWindow window = Minecraft.getInstance().getWindow();
		double d0 = window.getGuiScaleFactor();
		RenderSystem.enableScissor((int) (this.left * d0), (int) (window.getFramebufferHeight() - (this.bottom* d0)), (int)(this.width * d0), (int)(this.height * d0));
		super.renderList(p_238478_1_, p_238478_2_, p_238478_3_, p_238478_4_, p_238478_5_, p_238478_6_);
		RenderSystem.disableScissor();
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

	public void bumpCog(float force) {
		ConfigScreen.cogSpin.bump(3, force);
	}

	public static abstract class Entry extends ExtendedList.AbstractListEntry<Entry> {
		protected List<IGuiEventListener> listeners;

		protected Entry() {
			listeners = new ArrayList<>();
		}

		public void tick() {}

		public List<IGuiEventListener> getGuiListeners() {
			return listeners;
		}

		@Override
		public boolean mouseClicked(double x, double y, int button) {
			return getGuiListeners().stream().anyMatch(l -> l.mouseClicked(x, y, button));
		}

		@Override
		public boolean keyPressed(int code, int keyPressed_2_, int keyPressed_3_) {
			return getGuiListeners().stream().anyMatch(l -> l.keyPressed(code, keyPressed_2_, keyPressed_3_));
		}

		@Override
		public boolean charTyped(char ch, int code) {
			return getGuiListeners().stream().anyMatch(l -> l.charTyped(ch, code));
		}

		protected void setEditable(boolean b) {}

		protected boolean isForServer() {
			if (list == null)
				return false;
			return ((ConfigScreenList) list).isForServer;
		}
	}

	public static class LabeledEntry extends Entry {

		protected static final float labelWidthMult = 0.4f;

		protected TextStencilElement label;

		public LabeledEntry(String label) {
			this.label = new TextStencilElement(Minecraft.getInstance().fontRenderer, label);
		}

		@Override
		public void render(MatrixStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
			UIRenderHelper.streak(ms, 0, x, y+height/2, height - 10, getLabelWidth(width), 0x0);
			IFormattableTextComponent component = label.getComponent();
			if (Minecraft.getInstance().fontRenderer.getWidth(component) > getLabelWidth(width) - 10) {
				label.withText(Minecraft.getInstance().fontRenderer.trimToWidth(component, getLabelWidth(width) - 15).getString() + "...");
			}
			label.at(x + 5, y + height/2 - 4, 0).render(ms);
		}

		protected int getLabelWidth(int totalWidth) {
			return totalWidth;
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
