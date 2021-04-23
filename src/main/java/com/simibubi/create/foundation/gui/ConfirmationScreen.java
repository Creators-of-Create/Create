package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.ponder.PonderUI;
import com.simibubi.create.foundation.ponder.ui.PonderButton;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.Style;

public class ConfirmationScreen extends AbstractSimiScreen {

	private Screen source;
	private Consumer<Boolean> action = _success -> {};
	private List<ITextProperties> text = new ArrayList<>();
	private boolean centered = false;
	private int x;
	private int y;
	private int textWidth;
	private int textHeight;

	private PonderButton confirm;
	private PonderButton cancel;

	/*
	* Removes text lines from the back of the list
	* */
	public ConfirmationScreen removeTextLines(int amount) {
		if (amount > text.size())
			return clearText();

		text.subList(text.size() - amount, text.size()).clear();
		return this;
	}

	public ConfirmationScreen clearText() {
		this.text.clear();
		return this;
	}

	public ConfirmationScreen addText(ITextProperties text) {
		this.text.add(text);
		return this;
	}

	public ConfirmationScreen withText(ITextProperties text) {
		return clearText().addText(text);
	}

	public ConfirmationScreen at(int x, int y) {
		this.x = Math.max(x, 0);
		this.y = Math.max(y, 0);
		this.centered = false;
		return this;
	}

	public ConfirmationScreen centered() {
		this.centered = true;
		return this;
	}

	public ConfirmationScreen withAction(Consumer<Boolean> action) {
		this.action = action;
		return this;
	}

	public void open(@Nonnull Screen source) {
		this.source = source;
		Minecraft client = source.getMinecraft();
		this.init(client, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
		this.client.currentScreen = this;
	}

	@Override
	protected void init() {
		widgets.clear();

		ArrayList<ITextProperties> copy = new ArrayList<>(text);
		text.clear();
		copy.forEach(t -> text.addAll(client.fontRenderer.getTextHandler().wrapLines(t, 300, Style.EMPTY)));

		textHeight = text.size() * (client.fontRenderer.FONT_HEIGHT + 1) + 4;
		textWidth = 300;

		if (x + textWidth > width) {
			x = width - textWidth;
		}

		if (y + textHeight + 30 > height) {
			y = height - textHeight - 30;
		}

		if (centered) {
			x = width/2 - textWidth/2 - 2;
			y = height/2 - textHeight/2 - 16;
		}

		TextStencilElement confirmText = new TextStencilElement(client.fontRenderer, "Confirm").centered(true, true);
		confirm = new PonderButton(x + 4, y + textHeight + 2, (_$, _$$) -> accept(true), textWidth/2 - 10, 20)
				.showingUnscaled(confirmText);
		confirm.fade(1);

		TextStencilElement cancelText = new TextStencilElement(client.fontRenderer, "Cancel").centered(true, true);
		cancel = new PonderButton(x + textWidth/2 + 6, y + textHeight + 2, (_$, _$$) -> accept(false), textWidth/2 - 10, 20)
				.showingUnscaled(cancelText);
		cancel.fade(1);

		widgets.add(confirm);
		widgets.add(cancel);

	}

	@Override
	public void onClose() {
		accept(false);
	}

	private void accept(boolean success) {
		client.currentScreen = source;
		action.accept(success);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {

		PonderUI.renderBox(ms, x, y, textWidth, textHeight, false);
		int offset = client.fontRenderer.FONT_HEIGHT + 1;
		int lineY = y - offset;

		ms.push();
		ms.translate(0, 0, 200);

		for (ITextProperties line : text) {
			lineY = lineY + offset;

			if (line == null)
				continue;

			client.fontRenderer.draw(ms, line.getString(), x, lineY, 0xeaeaea);
		}

		ms.pop();

	}

	@Override
	protected void renderWindowBackground(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {

		UIRenderHelper.framebuffer.framebufferClear(Minecraft.IS_RUNNING_ON_MAC);
		UIRenderHelper.prepFramebufferSize();

		ms.push();
		//ms.translate(0, 0, -50);
		//ms.scale(1, 1, 0.01f);
		UIRenderHelper.framebuffer.bindFramebuffer(true);
		source.render(ms, mouseX, mouseY, partialTicks);
		UIRenderHelper.framebuffer.unbindFramebuffer();
		Minecraft.getInstance().getFramebuffer().bindFramebuffer(true);
		ms.pop();

		//RenderSystem.disableAlphaTest();
		RenderSystem.disableBlend();
		UIRenderHelper.drawFramebuffer(1);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		this.fillGradient(ms, 0, 0, this.width, this.height, 0x70101010, 0x80101010);
		//RenderSystem.enableAlphaTest();
	}

	@Override
	public void resize(@Nonnull Minecraft client, int width, int height) {
		super.resize(client, width, height);
		source.resize(client, width, height);
	}
}
