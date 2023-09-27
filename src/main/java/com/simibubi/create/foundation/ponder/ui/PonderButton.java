package com.simibubi.create.foundation.ponder.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.Theme.Key;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.element.RenderElement;
import com.simibubi.create.foundation.gui.widget.BoxWidget;
import com.simibubi.create.foundation.gui.widget.ElementWidget;
import com.simibubi.create.foundation.ponder.PonderTag;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class PonderButton extends BoxWidget {

	protected ItemStack item;
	protected PonderTag tag;
	protected KeyMapping shortcut;
	protected LerpedFloat flash = LerpedFloat.linear().startWithValue(0).chase(0, 0.1f, LerpedFloat.Chaser.EXP);

	public PonderButton(int x, int y) {
		this(x, y, 20, 20);
	}

	public PonderButton(int x, int y, int width, int height) {
		super(x, y, width, height);
		z = 420;
		paddingX = 2;
		paddingY = 2;
	}

	public <T extends PonderButton> T withShortcut(KeyMapping key) {
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
	protected void beforeRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.beforeRender(graphics, mouseX, mouseY, partialTicks);

		float flashValue = flash.getValue(partialTicks);
		if (flashValue > .1f) {
			float sin = 0.5f + 0.5f * Mth.sin((AnimationTickHolder.getTicks(true) + partialTicks) / 5f);
			sin *= flashValue;
			Color nc1 = new Color(255, 255, 255, Mth.clamp(gradientColor1.getAlpha() + 150, 0, 255));
			Color nc2 = new Color(155, 155, 155, Mth.clamp(gradientColor2.getAlpha() + 150, 0, 255));
			gradientColor1 = gradientColor1.mixWith(nc1, sin);
			gradientColor2 = gradientColor2.mixWith(nc2, sin);
		}
	}

	@Override
	public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.doRender(graphics, mouseX, mouseY, partialTicks);
		float fadeValue = fade.getValue();

		if (fadeValue < .1f)
			return;

		if (shortcut != null) {
			PoseStack ms = graphics.pose();
			ms.pushPose();
			ms.translate(0, 0, z + 10);
			graphics.drawCenteredString(Minecraft.getInstance().font, shortcut.getTranslatedKeyMessage(),
				getX() + width / 2 + 8, getY() + height - 6, Theme.c(Theme.Key.TEXT_DARKER)
					.scaleAlpha(fadeValue)
					.getRGB());
			ms.popPose();
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
