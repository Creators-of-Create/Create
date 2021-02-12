package com.simibubi.create.foundation.metadoc;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.metadoc.content.MetaDocIndex;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.LerpedFloat;
import com.simibubi.create.foundation.utility.LerpedFloat.Chaser;

import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.gui.GuiUtils;

public class MetaDocScreen extends AbstractSimiScreen {

	private List<MetaDocScene> scenes;
	private LerpedFloat fadeIn;

	private LerpedFloat lazyIndex;
	private int index = 0;

	public MetaDocScreen(List<MetaDocScene> scenes) {
		this.scenes = scenes;
		lazyIndex = LerpedFloat.linear()
			.startWithValue(index);
		fadeIn = LerpedFloat.linear()
			.startWithValue(0)
			.chase(1, .1f, Chaser.EXP);
	}

	@Override
	public void tick() {
		lazyIndex.tickChaser();
		fadeIn.tickChaser();
		scenes.get(index)
			.tick();
		float lazyIndexValue = lazyIndex.getValue();
		if (Math.abs(lazyIndexValue - index) > 1 / 512f)
			scenes.get(lazyIndexValue < index ? index - 1 : index + 1)
				.tick();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (scroll(delta > 0))
			return true;
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	protected boolean scroll(boolean forward) {
		int prevIndex = index;
		index = forward ? index + 1 : index - 1;
		index = MathHelper.clamp(index, 0, scenes.size() - 1);
		if (prevIndex != index && Math.abs(index - lazyIndex.getValue()) < 1.5f) {
			scenes.get(prevIndex)
				.fadeOut();
			scenes.get(index)
				.begin();
			lazyIndex.chase(index, 1 / 4f, Chaser.EXP);
			return true;
		} else
			index = prevIndex;
		return false;
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		RenderSystem.enableBlend();
		renderStories(partialTicks);
		renderWidgets(mouseX, mouseY, partialTicks);
	}

	protected void renderStories(float partialTicks) {
		renderStory(index, partialTicks);
		float lazyIndexValue = lazyIndex.getValue(partialTicks);
		if (Math.abs(lazyIndexValue - index) > 1 / 512f)
			renderStory(lazyIndexValue < index ? index - 1 : index + 1, partialTicks);
	}

	protected void renderStory(int i, float partialTicks) {
		SuperRenderTypeBuffer buffer = SuperRenderTypeBuffer.getInstance();
		MetaDocScene story = scenes.get(i);
		MatrixStack ms = new MatrixStack();
		double value = lazyIndex.getValue(partialTicks);
		double diff = i - value;
		double slide = MathHelper.lerp(diff * diff, 200, 600) * diff;

		ms.push();
		story.transform.updateScreenParams(width, height, slide);
		story.transform.apply(ms);
		story.renderScene(buffer, ms);
		buffer.draw();
		ms.pop();
	}

	protected void renderWidgets(int mouseX, int mouseY, float partialTicks) {
		float fade = fadeIn.getValue(partialTicks);
		float lazyIndexValue = lazyIndex.getValue(partialTicks);
		float indexDiff = Math.abs(lazyIndexValue - index);
		int textColor = 0xeeeeee;

		{
			int y = 34;
			drawString(font, "MetaDoc Experimental 0", 50, y, textColor);
			y += 10;
			drawString(font, "> " + scenes.get(index)
				.getTitle(), 50, y, ColorHelper.applyAlpha(textColor, 1 - indexDiff));
			y += 10;
			if (MetaDocIndex.EDITOR_MODE)
				drawString(font, "Mouse: " + mouseX + ", " + mouseY, 50, y, 0x8d8d8d);
		}

		// Scene overlay
		RenderSystem.pushMatrix();
		RenderSystem.translated(0, 0, 100);
		renderOverlay(index, partialTicks);
		if (indexDiff > 1 / 512f)
			renderOverlay(lazyIndexValue < index ? index - 1 : index + 1, partialTicks);
		RenderSystem.popMatrix();

		// Close button
		RenderSystem.pushMatrix();
		if (fade < fadeIn.getChaseTarget())
			RenderSystem.translated(0, (1 - fade) * 5, 0);
		int closeWidth = 24;
		int closeHeight = 24;
		int closeX = (width - closeWidth) / 2;
		int closeY = height - closeHeight - 31;
		boolean hovered = isMouseOver(mouseX, mouseY, closeX, closeY, closeWidth, closeHeight);
		renderBox(closeX, closeY, closeWidth, closeHeight, hovered);
		AllIcons.I_CONFIRM.draw(closeX + 4, closeY + 4);
		RenderSystem.popMatrix();
	}

	private void renderOverlay(int i, float partialTicks) {
		RenderSystem.pushMatrix();
		MetaDocScene story = scenes.get(i);
		MatrixStack ms = new MatrixStack();
		story.renderOverlay(this, ms, partialTicks);
		RenderSystem.popMatrix();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		int closeWidth = 24;
		int closeHeight = 24;
		int closeX = (width - closeWidth) / 2;
		int closeY = height - closeHeight - 31;
		if (isMouseOver(x, y, closeX, closeY, closeWidth, closeHeight)) {
			onClose();
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	public boolean keyPressed(int code, int p_keyPressed_2_, int p_keyPressed_3_) {
		GameSettings settings = Minecraft.getInstance().gameSettings;
		int sCode = settings.keyBindBack.getKey()
			.getKeyCode();
		int aCode = settings.keyBindLeft.getKey()
			.getKeyCode();
		int dCode = settings.keyBindRight.getKey()
			.getKeyCode();

		if (code == sCode) {
			onClose();
			return true;
		}

		if (code == aCode) {
			scroll(false);
			return true;
		}

		if (code == dCode) {
			scroll(true);
			return true;
		}

		return super.keyPressed(code, p_keyPressed_2_, p_keyPressed_3_);
	}
	
	public FontRenderer getFontRenderer() {
		return font;
	}

	protected boolean isMouseOver(double mouseX, double mouseY, int x, int y, int w, int h) {
		boolean hovered = !(mouseX < x || mouseX > x + w);
		hovered &= !(mouseY < y || mouseY > y + h);
		return hovered;
	}

	public void drawString(String s, int x, int y, int color) {
		drawString(font, s, x, y, color);
	}

	public void renderBox(int x, int y, int w, int h, boolean highlighted) {
		renderBox(x, y, w, h, 0xdd000000, highlighted ? 0x70ffffff : 0x30eebb00, highlighted ? 0x30ffffff : 0x10eebb00);
	}

	public void renderBox(int x, int y, int w, int h, int backgroundColor, int borderColorStart, int borderColorEnd) {
		int zLevel = 100;
		GuiUtils.drawGradientRect(zLevel, x - 3, y - 4, x + w + 3, y - 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(zLevel, x - 3, y + h + 3, x + w + 3, y + h + 4, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(zLevel, x - 3, y - 3, x + w + 3, y + h + 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(zLevel, x - 4, y - 3, x - 3, y + h + 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(zLevel, x + w + 3, y - 3, x + w + 4, y + h + 3, backgroundColor, backgroundColor);
		GuiUtils.drawGradientRect(zLevel, x - 3, y - 3 + 1, x - 3 + 1, y + h + 3 - 1, borderColorStart, borderColorEnd);
		GuiUtils.drawGradientRect(zLevel, x + w + 2, y - 3 + 1, x + w + 3, y + h + 3 - 1, borderColorStart,
			borderColorEnd);
		GuiUtils.drawGradientRect(zLevel, x - 3, y - 3, x + w + 3, y - 3 + 1, borderColorStart, borderColorStart);
		GuiUtils.drawGradientRect(zLevel, x - 3, y + h + 2, x + w + 3, y + h + 3, borderColorEnd, borderColorEnd);
	}

}
