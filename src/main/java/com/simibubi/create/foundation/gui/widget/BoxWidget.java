package com.simibubi.create.foundation.gui.widget;

import java.util.function.Function;

import com.simibubi.create.foundation.gui.Theme;
import com.simibubi.create.foundation.gui.Theme.Key;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.element.BoxElement;
import com.simibubi.create.foundation.gui.element.DelegatedStencilElement;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.client.gui.GuiGraphics;

public class BoxWidget extends ElementWidget {

	public static final Function<BoxWidget, DelegatedStencilElement.ElementRenderer> gradientFactory = (box) -> (ms, w, h, alpha) -> UIRenderHelper.angledGradient(ms, 90, w/2, -2, w + 4, h + 4, box.gradientColor1, box.gradientColor2);

	protected BoxElement box;

	protected Color customBorderTop;
	protected Color customBorderBot;
	protected Color customBackground;
	protected boolean animateColors = true;
	protected LerpedFloat colorAnimation = LerpedFloat.linear();

	protected Color gradientColor1, gradientColor2;
	private Color previousColor1, previousColor2;
	private Color colorTarget1 = Theme.c(getIdleTheme(), true).copy();
	private Color colorTarget2 = Theme.c(getIdleTheme(), false).copy();

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

	public <T extends BoxWidget> T withBorderColors(Couple<Color> colors) {
		this.customBorderTop = colors.getFirst();
		this.customBorderBot = colors.getSecond();
		updateColorsFromState();
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

	public <T extends BoxWidget> T withCustomBackground(Color color) {
		this.customBackground = color;
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

		gradientColor1 = Theme.c(getClickTheme(), true);
		gradientColor2 = Theme.c(getClickTheme(), false);
		startGradientAnimation(getColorForState(true), getColorForState(false), true, 0.15);
	}

	@Override
	protected void beforeRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.beforeRender(graphics, mouseX, mouseY, partialTicks);

		if (isHovered != wasHovered) {
			startGradientAnimation(
					getColorForState(true),
					getColorForState(false),
					isHovered
			);
		}

		if (colorAnimation.settled()) {
			gradientColor1 = colorTarget1;
			gradientColor2 = colorTarget2;
		} else {
			float animationValue = 1 - Math.abs(colorAnimation.getValue(partialTicks));
			gradientColor1 = Color.mixColors(previousColor1, colorTarget1, animationValue);
			gradientColor2 = Color.mixColors(previousColor2, colorTarget2, animationValue);
		}

	}

	@Override
	public void renderButton(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		float fadeValue = fade.getValue(partialTicks);
		if (fadeValue < .1f)
			return;

		box.withAlpha(fadeValue);
		box.withBackground(customBackground != null ? customBackground : Theme.c(Theme.Key.PONDER_BACKGROUND_TRANSPARENT))
				.gradientBorder(gradientColor1, gradientColor2)
				.at(getX(), getY(), z)
				.withBounds(width, height)
				.render(graphics);

		super.renderButton(graphics, mouseX, mouseY, partialTicks);

		wasHovered = isHovered;
	}

	@Override
	public boolean isMouseOver(double mX, double mY) {
		if (!active || !visible)
			return false;

		float padX = 2 + paddingX;
		float padY = 2 + paddingY;

		return getX() - padX <= mX && getY() - padY <= mY && mX < getX() + padX + width && mY < getY() + padY + height;
	}
	
	@Override
	protected boolean clicked(double pMouseX, double pMouseY) {
		if (!active || !visible)
			return false;
		return isMouseOver(pMouseX, pMouseY);
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
		startGradientAnimation(c1, c2, positive, 0.6);
	}

	private Color getColorForState(boolean first) {
		if (!active)
			return Theme.p(getDisabledTheme()).get(first);

		if (isHovered) {
			if (first)
				return customBorderTop != null ? customBorderTop.darker() : Theme.c(getHoverTheme(), true);
			else
				return customBorderBot != null ? customBorderBot.darker() : Theme.c(getHoverTheme(), false);
		}

		if (first)
			return customBorderTop != null ? customBorderTop : Theme.c(getIdleTheme(), true);
		else
			return customBorderBot != null ? customBorderBot : Theme.c(getIdleTheme(), false);
	}

	public Key getDisabledTheme() {
		return Theme.Key.BUTTON_DISABLE;
	}

	public Key getIdleTheme() {
		return Theme.Key.BUTTON_IDLE;
	}

	public Key getHoverTheme() {
		return Theme.Key.BUTTON_HOVER;
	}

	public Key getClickTheme() {
		return Theme.Key.BUTTON_CLICK;
	}

}
