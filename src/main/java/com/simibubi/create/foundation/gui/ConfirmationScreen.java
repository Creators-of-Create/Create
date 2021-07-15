package com.simibubi.create.foundation.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.backend.gl.versioned.GlCompat;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.widgets.BoxWidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.FramebufferConstants;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.Style;

public class ConfirmationScreen extends AbstractSimiScreen {

	private Screen source;
	private Consumer<Response> action = _success -> {
	};
	private List<ITextProperties> text = new ArrayList<>();
	private boolean centered = false;
	private int x;
	private int y;
	private int textWidth;
	private int textHeight;
	private boolean tristate;

	private BoxWidget confirm;
	private BoxWidget confirmDontSave;
	private BoxWidget cancel;
	private BoxElement textBackground;

	public enum Response {
		Confirm, ConfirmDontSave, Cancel
	}

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
		this.action = r -> action.accept(r == Response.Confirm);
		return this;
	}

	public ConfirmationScreen withThreeActions(Consumer<Response> action) {
		this.action = action;
		this.tristate = true;
		return this;
	}

	public void open(@Nonnull Screen source) {
		this.source = source;
		Minecraft client = source.getMinecraft();
		this.init(client, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
		this.minecraft.screen = this;
	}

	@Override
	public void tick() {
		super.tick();
		confirm.tick();
		cancel.tick();
	}

	@Override
	protected void init() {
		widgets.clear();

		ArrayList<ITextProperties> copy = new ArrayList<>(text);
		text.clear();
		copy.forEach(t -> text.addAll(minecraft.font.getSplitter().splitLines(t, 300, Style.EMPTY)));

		textHeight = text.size() * (minecraft.font.lineHeight + 1) + 4;
		textWidth = 300;

		if (centered) {
			x = width/2 - textWidth/2 - 2;
			y = height/2 - textHeight/2 - 16;
		} else {
			x = Math.max(0, x - textWidth / 2);
			y = Math.max(0, y -= textHeight);
		}

		if (x + textWidth > width) {
			x = width - textWidth;
		}

		if (y + textHeight + 30 > height) {
			y = height - textHeight - 30;
		}

		int buttonX = x + textWidth / 2 - 6 - (int) (70 * (tristate ? 1.5f : 1));

		TextStencilElement confirmText =
				new TextStencilElement(minecraft.font, tristate ? "Save" : "Confirm").centered(true, true);
		confirm = new BoxWidget(buttonX, y + textHeight + 6, 70, 16).withCallback(() -> accept(Response.Confirm));
		confirm.showingElement(confirmText.withElementRenderer(BoxWidget.gradientFactory.apply(confirm)));
		widgets.add(confirm);

		buttonX += 12 + 70;

		if (tristate) {
			TextStencilElement confirmDontSaveText =
					new TextStencilElement(minecraft.font, "Don't Save").centered(true, true);
			confirmDontSave =
					new BoxWidget(buttonX, y + textHeight + 6, 70, 16).withCallback(() -> accept(Response.ConfirmDontSave));
			confirmDontSave.showingElement(
					confirmDontSaveText.withElementRenderer(BoxWidget.gradientFactory.apply(confirmDontSave)));
			widgets.add(confirmDontSave);
			buttonX += 12 + 70;
		}

		TextStencilElement cancelText = new TextStencilElement(minecraft.font, "Cancel").centered(true, true);
		cancel = new BoxWidget(buttonX, y + textHeight + 6, 70, 16)
				.withCallback(() -> accept(Response.Cancel));
		cancel.showingElement(cancelText.withElementRenderer(BoxWidget.gradientFactory.apply(cancel)));
		widgets.add(cancel);

		textBackground = new BoxElement()
				.gradientBorder(Theme.p(Theme.Key.BUTTON_DISABLE))
				.withBounds(width + 10, textHeight + 35)
				.at(-5, y - 5);

	}

	@Override
	public void onClose() {
		accept(Response.Cancel);
	}

	private void accept(Response success) {
		minecraft.screen = source;
		action.accept(success);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {

		textBackground.render(ms);
		int offset = minecraft.font.lineHeight + 1;
		int lineY = y - offset;

		ms.pushPose();
		ms.translate(0, 0, 200);

		for (ITextProperties line : text) {
			lineY = lineY + offset;
			if (line == null)
				continue;
			int textX = x;
			if (text.size() == 1)
				x = (width - minecraft.font.width(line)) / 2;
			minecraft.font.draw(ms, line.getString(), textX, lineY, 0xeaeaea);
		}

		ms.popPose();

	}

	@Override
	protected void renderWindowBackground(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		endFrame();

		source.render(ms, 0, 0, 10); // zero mouse coords to prevent further tooltips

		prepareFrame();

		this.fillGradient(ms, 0, 0, this.width, this.height, 0x70101010, 0x80101010);
	}

	@Override
	protected void prepareFrame() {
		Framebuffer thisBuffer = UIRenderHelper.framebuffer;
		Framebuffer mainBuffer = Minecraft.getInstance().getMainRenderTarget();

		GlCompat functions = Backend.getInstance().compat;
		functions.fbo.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, mainBuffer.frameBufferId);
		functions.fbo.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, thisBuffer.frameBufferId);
		functions.blit.blitFramebuffer(0, 0, mainBuffer.viewWidth, mainBuffer.viewHeight, 0, 0, mainBuffer.viewWidth, mainBuffer.viewHeight, GL30.GL_COLOR_BUFFER_BIT, GL20.GL_LINEAR);

		functions.fbo.bindFramebuffer(FramebufferConstants.GL_FRAMEBUFFER, thisBuffer.frameBufferId);
		GL11.glClear(GL30.GL_STENCIL_BUFFER_BIT | GL30.GL_DEPTH_BUFFER_BIT);

	}

	@Override
	protected void endFrame() {

		Framebuffer thisBuffer = UIRenderHelper.framebuffer;
		Framebuffer mainBuffer = Minecraft.getInstance().getMainRenderTarget();

		GlCompat functions = Backend.getInstance().compat;
		functions.fbo.bindFramebuffer(GL30.GL_READ_FRAMEBUFFER, thisBuffer.frameBufferId);
		functions.fbo.bindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, mainBuffer.frameBufferId);
		functions.blit.blitFramebuffer(0, 0, mainBuffer.viewWidth, mainBuffer.viewHeight, 0, 0, mainBuffer.viewWidth, mainBuffer.viewHeight, GL30.GL_COLOR_BUFFER_BIT, GL20.GL_LINEAR);

		functions.fbo.bindFramebuffer(FramebufferConstants.GL_FRAMEBUFFER, mainBuffer.frameBufferId);
	}

	@Override
	public void resize(@Nonnull Minecraft client, int width, int height) {
		super.resize(client, width, height);
		source.resize(client, width, height);
	}

	@Override
	public boolean isPauseScreen() {
		return true;
	}
}
