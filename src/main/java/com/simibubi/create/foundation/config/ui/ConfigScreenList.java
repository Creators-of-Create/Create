package com.simibubi.create.foundation.config.ui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.config.ui.entries.NumberEntry;
import com.simibubi.create.foundation.gui.TextStencilElement;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.UIRenderHelper;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class ConfigScreenList extends ExtendedList<ConfigScreenList.Entry> {

	public static TextFieldWidget currentText;

	public boolean isForServer = false;

	public ConfigScreenList(Minecraft client, int width, int height, int top, int bottom, int elementHeight) {
		super(client, width, height, top, bottom, elementHeight);
		func_244605_b(false);
		func_244606_c(false);
		setRenderSelection(false);
		currentText = null;
		headerHeight = 3;
	}

	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		//render tmp background
		//fill(ms, left, top, left + width, top + height, 0x10_000000);

		UIRenderHelper.angledGradient(ms, 90, left + width / 2, top, width, 5, 0x60_000000, 0x0);
		UIRenderHelper.angledGradient(ms, -90, left + width / 2, bottom, width, 5, 0x60_000000, 0x0);
		UIRenderHelper.angledGradient(ms, 0, left, top + height / 2, height, 5, 0x60_000000, 0x0);
		UIRenderHelper.angledGradient(ms, 180, right, top + height / 2, height, 5, 0x60_000000, 0x0);

		super.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void renderList(MatrixStack p_238478_1_, int p_238478_2_, int p_238478_3_, int p_238478_4_, int p_238478_5_, float p_238478_6_) {
		MainWindow window = Minecraft.getInstance().getWindow();
		double d0 = window.getGuiScaleFactor();
		RenderSystem.enableScissor((int) (this.left * d0), (int) (window.getFramebufferHeight() - (this.bottom * d0)), (int) (this.width * d0), (int) (this.height * d0));
		super.renderList(p_238478_1_, p_238478_2_, p_238478_3_, p_238478_4_, p_238478_5_, p_238478_6_);
		RenderSystem.disableScissor();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		children().stream().filter(e -> e instanceof NumberEntry<?>).forEach(e -> e.mouseClicked(x, y, button));

		return super.mouseClicked(x, y, button);
	}

	@Override
	public int getRowWidth() {
		return width - 16;
	}

	@Override
	protected int getScrollbarPositionX() {
		return left + this.width - 6;
	}

	public void tick() {
		//children().forEach(Entry::tick);
		for(int i = 0; i < getItemCount(); ++i) {
			int top = this.getRowTop(i);
			int bot = top + itemHeight;
			if (bot >= this.top && top <= this.bottom)
				this.getEntry(i).tick();
		}

	}

	public void bumpCog(float force) {
		ConfigScreen.cogSpin.bump(3, force);
	}

	public static abstract class Entry extends ExtendedList.AbstractListEntry<Entry> {
		protected List<IGuiEventListener> listeners;

		protected Entry() {
			listeners = new ArrayList<>();
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

		public void tick() {}

		public List<IGuiEventListener> getGuiListeners() {
			return listeners;
		}

		protected void setEditable(boolean b) {}
	}

	public static class LabeledEntry extends Entry {

		protected static final float labelWidthMult = 0.4f;

		protected TextStencilElement label;
		protected List<ITextComponent> labelTooltip;
		protected String unit = null;

		public LabeledEntry(String label) {
			this.label = new TextStencilElement(Minecraft.getInstance().fontRenderer, label);
			this.label.withElementRenderer((ms, width, height, alpha) -> UIRenderHelper.angledGradient(ms, 0, 0, height / 2, height, width, Theme.p(Theme.Key.TEXT_ACCENT_STRONG)));
			labelTooltip = new ArrayList<>();
		}

		@Override
		public void render(MatrixStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
			UIRenderHelper.streak(ms, 0, x, y + height / 2, height - 6, width, 0xdd_000000);
			IFormattableTextComponent component = label.getComponent();
			FontRenderer font = Minecraft.getInstance().fontRenderer;
			if (font.getWidth(component) > getLabelWidth(width) - 10) {
				label.withText(font.trimToWidth(component, getLabelWidth(width) - 15).getString() + "...");
			}
			if (unit != null) {
				int unitWidth = font.getStringWidth(unit);
				font.draw(ms, unit, x + getLabelWidth(width) - unitWidth - 5, y + height / 2 + 2, Theme.i(Theme.Key.TEXT_DARKER));
				label.at(x + 10, y + height / 2 - 10, 0).render(ms);
			} else {
				label.at(x + 10, y + height / 2 - 4, 0).render(ms);
			}


			if (mouseX > x && mouseX < x + getLabelWidth(width) && mouseY > y + 5 && mouseY < y + height - 5) {
				List<ITextComponent> tooltip = getLabelTooltip();
				if (tooltip.isEmpty())
					return;

				GL11.glDisable(GL11.GL_SCISSOR_TEST);
				Screen screen = Minecraft.getInstance().currentScreen;
				ms.push();
				ms.translate(0, 0, 400);
				GuiUtils.drawHoveringText(ms, tooltip, mouseX, mouseY, screen.width, screen.height, 300, font);
				ms.pop();
				GL11.glEnable(GL11.GL_SCISSOR_TEST);
			}
		}

		public List<ITextComponent> getLabelTooltip() {
			return labelTooltip;
		}

		protected int getLabelWidth(int totalWidth) {
			return totalWidth;
		}
	}
}
