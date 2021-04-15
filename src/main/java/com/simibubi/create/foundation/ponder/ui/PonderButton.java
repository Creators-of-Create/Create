package com.simibubi.create.foundation.ponder.ui;

import java.util.function.BiConsumer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.IScreenRenderable;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;
import com.simibubi.create.foundation.ponder.PonderUI;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;

public class PonderButton extends AbstractSimiWidget {

	private IScreenRenderable icon;
	private ItemStack item;
	protected boolean pressed;
	private BiConsumer<Integer, Integer> onClick;
	private int xFadeModifier;
	private int yFadeModifier;
	private float fade;
	private KeyBinding shortcut;
	private LerpedFloat flash;
	private Couple<Integer> customPassiveBorder;

	public static final int SIZE = 20;

	public PonderButton(int x, int y, BiConsumer<Integer, Integer> onClick, int width, int height) {
		super(x, y, width, height);
		this.onClick = onClick;
		flash = LerpedFloat.linear()
			.startWithValue(0);
	}

	public PonderButton(int x, int y, BiConsumer<Integer, Integer> onClick) {
		this(x, y, onClick, SIZE, SIZE);
	}

	public PonderButton(int x, int y, Runnable onClick) {
		this(x, y, ($, $$) -> onClick.run());
	}

	public PonderButton showing(IScreenRenderable icon) {
		this.icon = icon;
		return this;
	}

	public PonderButton showing(ItemStack item) {
		this.item = item;
		return this;
	}

	public PonderButton customColors(int start, int end) {
		this.customPassiveBorder = Couple.create(start, end);
		return this;
	}

	public PonderButton shortcut(KeyBinding key) {
		this.shortcut = key;
		return this;
	}

	public PonderButton fade(int xModifier, int yModifier) {
		this.xFadeModifier = xModifier;
		this.yFadeModifier = yModifier;
		return this;
	}

	public void fade(float fade) {
		this.fade = fade;
	}

	public void flash() {
		float value = flash.getValue();
		flash.setValue(value + (1 - value) * .2f);
	}

	public void dim() {
		float value = flash.getValue();
		flash.setValue(value * .5f);
	}

	@Override
	public void renderButton(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		if (!visible)
			return;
		if (fade < .1f)
			return;

		hovered = isMouseOver(mouseX, mouseY) && fade > .75f;

		ms.push();
		RenderSystem.disableDepthTest();
		if (fade < 1)
			ms.translate((1 - fade) * -5 * xFadeModifier, (1 - fade) * -5 * yFadeModifier, 0);

		float flashValue = flash.getValue(partialTicks);
		if (flashValue > .1f)
			fade *= 3 * flashValue + Math.sin((PonderUI.ponderTicks + partialTicks) / 6);

		int backgroundColor = ColorHelper.applyAlpha(0xdd000000, fade);
		int borderColorStart = customPassiveBorder != null ? customPassiveBorder.getFirst() : hovered ? 0x70ffffff : 0x40aa9999;
		int borderColorEnd = customPassiveBorder != null ? customPassiveBorder.getSecond() : hovered ? 0x30ffffff : 0x20aa9999;
		borderColorStart = ColorHelper.applyAlpha(borderColorStart, fade);
		borderColorEnd = ColorHelper.applyAlpha(borderColorEnd, fade);

		ms.translate(0, 0, 400);
		PonderUI.renderBox(ms, x, y, width, height, backgroundColor, borderColorStart, borderColorEnd);
		ms.translate(0, 0, 100);

		if (icon != null) {
			RenderSystem.enableBlend();
			RenderSystem.color4f(1, 1, 1, fade);
			ms.push();
			ms.translate(x + 2, y + 2, 0);
			ms.scale((width - 4) / 16f, (height - 4) / 16f, 1);
			icon.draw(ms, this, 0, 0);
			ms.pop();
		}
		if (item != null) {
			ms.push();
			ms.translate(0, 0, -100);
			GuiGameElement.of(item)
				.at(x - 2, y - 2)
				.scale(1.5f)
				.render(ms);
			ms.pop();
		}
		if (shortcut != null)
			drawCenteredText(ms, Minecraft.getInstance().fontRenderer, shortcut.getBoundKeyLocalizedText(), x + width / 2 + 8,
				y + height - 6, ColorHelper.applyAlpha(0xff606060, fade));

		ms.pop();
	}

	public void runCallback(double mouseX, double mouseY) {
		onClick.accept((int) mouseX, (int) mouseY);
	}

	@Override
	public void onClick(double p_onClick_1_, double p_onClick_3_) {
		super.onClick(p_onClick_1_, p_onClick_3_);
		this.pressed = true;
	}

	@Override
	public void onRelease(double p_onRelease_1_, double p_onRelease_3_) {
		super.onRelease(p_onRelease_1_, p_onRelease_3_);
		this.pressed = false;
	}

	/*public void setToolTip(String text) {
		toolTip.clear();
		toolTip.add(text);
	}*/

	public ItemStack getItem() {
		return item;
	}

	@Override
	public boolean isMouseOver(double x, double y) {
		double m = 4;
		x = Math.floor(x);
		y = Math.floor(y);
		return active && visible
			&& !(x < this.x - m || x > this.x + width + m - 1 || y < this.y - m || y > this.y + height + m - 1);
	}
}
