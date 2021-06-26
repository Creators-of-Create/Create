package com.simibubi.create.foundation.ponder.ui;

import java.awt.*;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.RenderElement;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.Theme.Key;
import com.simibubi.create.foundation.gui.widgets.BoxWidget;
import com.simibubi.create.foundation.gui.widgets.ElementWidget;
import com.simibubi.create.foundation.ponder.content.PonderTag;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class PonderButton extends BoxWidget {

	protected ItemStack item;
	protected PonderTag tag;
	protected KeyBinding shortcut;
	protected LerpedFloat flash = LerpedFloat.linear().startWithValue(0).chase(0, 0.1f, LerpedFloat.Chaser.EXP);

	public PonderButton(int x, int y) {
		this(x, y, 20, 20);
	}

	public PonderButton(int x, int y, int width, int height) {
		super(x, y, width, height);
		z = 400;
		paddingX = 2;
		paddingY = 2;
	}

	public <T extends PonderButton> T withShortcut(KeyBinding key) {
		this.shortcut = key;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends PonderButton> T showingTag(PonderTag tag) {
		return showing(this.tag = tag);
	}
	
	public <T extends PonderButton> T showing(ItemStack item) {
		this.item = item;
		return super.showingElement(GuiGameElement.of(item)
				.scale(1.5f)
				.at(-4, -4));
	}

	@Override
	public <T extends ElementWidget> T showingElement(RenderElement element) {
		return super.showingElement(element);
	}

	public void flash() {
		flash.updateChaseTarget(1);
	}

	public void dim() {
		flash.updateChaseTarget(0);
	}

	@Override
	public void tick() {
		super.tick();
		flash.tickChaser();
	}

	@Override
	protected void beforeRender(@Nonnull MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		super.beforeRender(ms, mouseX, mouseY, partialTicks);

		float flashValue = flash.getValue(partialTicks);
		if (flashValue > .1f) {
			float sin = 0.5f + 0.5f * MathHelper.sin((AnimationTickHolder.getTicks(true) + partialTicks) / 5f);
			sin *= flashValue;
			Color c1 = gradientColor1;
			Color c2 = gradientColor2;
			Color nc1 = new Color(255, 255, 255, MathHelper.clamp(c1.getAlpha() + 150, 0, 255));
			Color nc2 = new Color(155, 155, 155, MathHelper.clamp(c2.getAlpha() + 150, 0, 255));
			gradientColor1 = ColorHelper.mixColors(c1, nc1, sin);
			gradientColor2 = ColorHelper.mixColors(c2, nc2, sin);
		}
	}

	@Override
	public void renderButton(@Nonnull MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		super.renderButton(ms, mouseX, mouseY, partialTicks);
		float fadeValue = fade.getValue();

		if (fadeValue < .1f)
			return;

		if (shortcut != null) {
			ms.translate(0, 0, z + 50);
			drawCenteredText(ms, Minecraft.getInstance().fontRenderer, shortcut.getBoundKeyLocalizedText(), x + width / 2 + 8, y + height - 6, ColorHelper.applyAlpha(Theme.i(Theme.Key.TEXT_DARKER), fadeValue));
		}
	}

	public ItemStack getItem() {
		return item;
	}
	
	public PonderTag getTag() {
		return tag;
	}

	@Override
	public Key getDisabledTheme() {
		return Theme.Key.PONDER_BUTTON_DISABLE;
	}

	@Override
	public Key getIdleTheme() {
		return Theme.Key.PONDER_BUTTON_IDLE;
	}

	@Override
	public Key getHoverTheme() {
		return Theme.Key.PONDER_BUTTON_HOVER;
	}

	@Override
	public Key getClickTheme() {
		return Theme.Key.PONDER_BUTTON_CLICK;
	}

}
