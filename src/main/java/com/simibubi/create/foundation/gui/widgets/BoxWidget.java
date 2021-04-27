package com.simibubi.create.foundation.gui.widgets;

import java.awt.Color;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.BoxElement;
import com.simibubi.create.foundation.gui.DelegatedStencilElement;
import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

public class BoxWidget extends ElementWidget {

	public static final Function<BoxWidget, DelegatedStencilElement.ElementRenderer> gradientFactory = (box) -> (ms, w, h, alpha) -> UIRenderHelper.angledGradient(ms, 90, w/2, -2, w + 4, h + 4, box.gradientColor1.getRGB(), box.gradientColor2.getRGB());

	protected BoxElement box;

	protected Color customBorderTop;
	protected Color customBorderBot;
	protected boolean animateColors = true;
	protected LerpedFloat colorAnimation = LerpedFloat.linear();
	protected Color gradientColor1, gradientColor2;
	private Color colorTarget1 = Theme.c(Theme.Key.BUTTON_IDLE_1), colorTarget2 = Theme.c(Theme.Key.BUTTON_IDLE_2);
	private Color previousColor1, previousColor2;

	public BoxWidget() {
		this(0, 0);
	}

	public BoxWidget(int x, int y) {
		this(x, y, 16, 16);
	}

	public BoxWidget(int x, int y, int width, int height) {
		super(x, y, width, height);
		box = new BoxElement()
				.at(x, y)
				.withBounds(width, height);
		gradientColor1 = colorTarget1;
		gradientColor2 = colorTarget2;
	}

	public <T extends BoxWidget> T withBounds(int width, int height) {
		this.width = width;
		this.height = height;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends BoxWidget> T withBorderColors(Color top, Color bot) {
		this.customBorderTop = top;
		this.customBorderBot = bot;
		updateColorsFromState();
		//noinspection unchecked
		return (T) this;
	}

	public <T extends BoxWidget> T animateColors(boolean b) {
		this.animateColors = b;
		//noinspection unchecked
		return (T) this;
	}

	@Override
	public void tick() {
		super.tick();
		colorAnimation.tickChaser();
	}

	@Override
	public void onClick(double x, double y) {
		super.onClick(x, y);

		gradientColor1 = Theme.c(Theme.Key.BUTTON_CLICK_1);
		gradientColor2 = Theme.c(Theme.Key.BUTTON_CLICK_2);
		startGradientAnimation(getColorForState(true), getColorForState(false), true, 0.15);
	}

	@Override
	protected void beforeRender(@Nonnull MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		super.beforeRender(ms, mouseX, mouseY, partialTicks);

		if (hovered != wasHovered) {
			startGradientAnimation(
					getColorForState(true),
					getColorForState(false),
					hovered
			);
		}

		if (colorAnimation.settled()) {
			gradientColor1 = colorTarget1;
			gradientColor2 = colorTarget2;
		} else {
			float animationValue = 1 - Math.abs(colorAnimation.getValue(partialTicks));
			gradientColor1 = ColorHelper.mixColors(previousColor1, colorTarget1, animationValue);
			gradientColor2 = ColorHelper.mixColors(previousColor2, colorTarget2, animationValue);
		}

	}

	@Override
	public void renderButton(@Nonnull MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		float fadeValue = fade.getValue(partialTicks);
		if (fadeValue < .1f)
			return;

		box.withAlpha(fadeValue);
		box.withBackground(Theme.c(Theme.Key.PONDER_BACKGROUND))
				.gradientBorder(gradientColor1, gradientColor2)
				.at(x, y, z)
				.withBounds(width, height)
				.render(ms);

		super.renderButton(ms, mouseX, mouseY, partialTicks);

		wasHovered = hovered;
	}

	@Override
	public boolean isMouseOver(double mX, double mY) {
		if (!active || !visible)
			return false;

		return
				x - 4 <= mX &&
				y - 4 <= mY &&
				mX <= x + 4 + width &&
				mY <= y + 4 + height;
	}

	public BoxElement getBox() {
		return box;
	}

	public void updateColorsFromState() {
		colorTarget1 = getColorForState(true);
		colorTarget2 = getColorForState(false);
	}

	public void animateGradientFromState() {
		startGradientAnimation(
				getColorForState(true),
				getColorForState(false),
				true
		);
	}

	private void startGradientAnimation(Color c1, Color c2, boolean positive, double expSpeed) {
		if (!animateColors)
			return;

		colorAnimation.startWithValue(positive ? 1 : -1);
		colorAnimation.chase(0, expSpeed, LerpedFloat.Chaser.EXP);
		colorAnimation.tickChaser();

		previousColor1 = gradientColor1;
		previousColor2 = gradientColor2;

		colorTarget1 = c1;
		colorTarget2 = c2;
	}

	private void startGradientAnimation(Color c1, Color c2, boolean positive) {
		startGradientAnimation(c1, c2, positive, 0.3);
	}

	private Color getColorForState(boolean first) {
		if (!active)
			return first ? Theme.c(Theme.Key.BUTTON_DISABLE_1) : Theme.c(Theme.Key.BUTTON_DISABLE_2);

		if (hovered) {
			if (first)
				return customBorderTop != null ? customBorderTop.darker() : Theme.c(Theme.Key.BUTTON_HOVER_1);
			else
				return customBorderBot != null ? customBorderBot.darker() : Theme.c(Theme.Key.BUTTON_HOVER_2);
		}

		if (first)
			return customBorderTop != null ? customBorderTop : Theme.c(Theme.Key.BUTTON_IDLE_1);
		else
			return customBorderBot != null ? customBorderBot : Theme.c(Theme.Key.BUTTON_IDLE_2);
	}
}
