package com.simibubi.create.foundation.ponder.ui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.AbstractSimiWidget;
import com.simibubi.create.foundation.ponder.PonderUI;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.LerpedFloat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;

public class PonderButton extends AbstractSimiWidget {

	private AllIcons icon;
	private ItemStack item;
	protected boolean pressed;
	private Runnable onClick;
	private int xFadeModifier;
	private int yFadeModifier;
	private float fade;
	private KeyBinding shortcut;
	private LerpedFloat flash;

	public static final int SIZE = 20;

	public PonderButton(int x, int y, Runnable onClick) {
		super(x, y, SIZE, SIZE);
		this.onClick = onClick;
		flash = LerpedFloat.linear()
			.startWithValue(0);
	}

	public PonderButton showing(AllIcons icon) {
		this.icon = icon;
		return this;
	}

	public PonderButton showing(ItemStack item) {
		this.item = item;
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
	public void renderButton(int mouseX, int mouseY, float partialTicks) {
		if (!visible)
			return;
		if (fade < .1f)
			return;

		isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height && fade > .75f;

		RenderSystem.pushMatrix();
		RenderSystem.disableDepthTest();
		if (fade < 1)
			RenderSystem.translated((1 - fade) * -5 * xFadeModifier, (1 - fade) * -5 * yFadeModifier, 0);

		float flashValue = flash.getValue(partialTicks);
		if (flashValue > .1f)
			fade *= 3 * flashValue + (Math.sin(AnimationTickHolder.getRenderTick() / 6)) / 1f;

		int backgroundColor = ColorHelper.applyAlpha(0xdd000000, fade);
		int borderColorStart = ColorHelper.applyAlpha(isHovered ? 0x70ffffff : 0x40aa9999, fade);
		int borderColorEnd = ColorHelper.applyAlpha(isHovered ? 0x30ffffff : 0x20aa9999, fade);

		PonderUI.renderBox(x, y, width, height, backgroundColor, borderColorStart, borderColorEnd);
		RenderSystem.translated(0, 0, 800);

		if (icon != null) {
			RenderSystem.enableBlend();
			RenderSystem.color4f(1, 1, 1, fade);
			icon.draw(this, x + 2, y + 2);
		}
		if (item != null) {
			GuiGameElement.of(item)
				.at(x - 2, y - 2)
				.scale(1.5f)
				.render();
		}
		if (shortcut != null)
			drawCenteredString(Minecraft.getInstance().fontRenderer, shortcut.getLocalizedName(), x + SIZE / 2 + 8,
				y + SIZE - 6, ColorHelper.applyAlpha(0xff606060, fade));

		RenderSystem.popMatrix();
	}

	public void runCallback() {
		onClick.run();
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

	public void setToolTip(String text) {
		toolTip.clear();
		toolTip.add(text);
	}

}
