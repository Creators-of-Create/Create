package com.simibubi.create.foundation.config.ui;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.CombinedStencilElement;
import com.simibubi.create.foundation.gui.StencilElement;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.widgets.StencilWidget;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

public class ConfigButton extends StencilWidget {

	LerpedFloat colorAnimation = LerpedFloat.linear();
	protected int gradientColor1 = Palette.button_idle_1, gradientColor2 = Palette.button_idle_2;
	private int colorTarget1, colorTarget2;
	private int previousColor1, previousColor2;

	protected boolean wasHovered;

	public static ConfigButton createFromStencilElement(int x, int y, StencilElement text) {
		ConfigButton button = new ConfigButton(x, y);
		StencilElement box  = new StencilElement() {
			@Override
			protected void renderStencil(MatrixStack ms) {
				fill(ms, 0, 0     , width    , 0      + 1, 0xff_000000);
				fill(ms, 0, height, width + 1, height + 1, 0xff_000000);
				fill(ms, 0    , 0, 0     + 1, height, 0xff_000000);
				fill(ms, width, 0, width + 1, height, 0xff_000000);
			}

			@Override
			protected void renderElement(MatrixStack ms) {
				UIRenderHelper.angledGradient(ms, 0, 0, height/2, height+2, width+2, button.gradientColor1, button.gradientColor2);
			}
		};
		button.stencilElement = CombinedStencilElement.of(box, text);
		return button;
	}

	protected ConfigButton(int x, int y) {
		super(x, y);
	}

	protected ConfigButton(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public ConfigButton(int x, int y, int width, int height, StencilElement stencilElement) {
		super(x, y, width, height, stencilElement);
	}

	public ConfigButton withBounds(int width, int height) {
		this.width = width;
		this.height = height;
		return this;
	}

	public void tick() {
		colorAnimation.tickChaser();
	}

	@Override
	public void onClick(double x, double y) {
		runCallback(x, y);

		gradientColor1 = Palette.button_click_1;
		gradientColor2 = Palette.button_click_2;
		startGradientAnimation(Palette.getColorForButtonState(true, active, hovered), Palette.getColorForButtonState(false, active, hovered), true, 0.15);
	}

	@Override
	public void renderButton(@Nonnull MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		//update hover status
		//hovered = isMouseOver(mouseX, mouseY);
		if (hovered != wasHovered) {
			startGradientAnimation(
					Palette.getColorForButtonState(true, active, hovered),
					Palette.getColorForButtonState(false, active, hovered),
					hovered
			);
		}

		//update color animations
		if (!colorAnimation.settled()) {
			float animationValue = 1 - Math.abs(colorAnimation.getValue(partialTicks));
			gradientColor1 = ColorHelper.mixAlphaColors(previousColor1, colorTarget1, animationValue);
			gradientColor2 = ColorHelper.mixAlphaColors(previousColor2, colorTarget2, animationValue);
		}

		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		fill(ms, x, y, x + width, y + height, 0x30_ffffff);
		RenderSystem.defaultBlendFunc();
		super.renderButton(ms, mouseX, mouseY, partialTicks);
		wasHovered = hovered;
	}

	public void updateColorsFromState() {
		gradientColor1 = Palette.getColorForButtonState(true, active, hovered);
		gradientColor2 = Palette.getColorForButtonState(false, active, hovered);
	}

	public void animateGradientFromState() {
		startGradientAnimation(
				Palette.getColorForButtonState(true, active, hovered),
				Palette.getColorForButtonState(false, active, hovered),
				true
		);
	}

	private void startGradientAnimation(int c1, int c2, boolean positive, double expSpeed) {
		colorAnimation.startWithValue(positive ? 1 : -1);
		colorAnimation.chase(0, expSpeed, LerpedFloat.Chaser.EXP);

		previousColor1 = gradientColor1;
		previousColor2 = gradientColor2;

		colorTarget1 = c1;
		colorTarget2 = c2;
	}

	private void startGradientAnimation(int c1, int c2, boolean positive) {
		startGradientAnimation(c1, c2, positive, 0.3);
	}

	public static class Palette {
		public static int button_idle_1 = 0xff_c0c0ff;
		public static int button_idle_2 = 0xff_7b7ba3;

		public static int button_hover_1 = 0xff_7b7ba3;
		public static int button_hover_2 = 0xff_616192;

		public static int button_click_1 = 0xff_4b4bff;
		public static int button_click_2 = 0xff_3b3bdd;

		public static int button_disable_1 = 0xff_909090;
		public static int button_disable_2 = 0xff_606060;

		public static int getColorForButtonState(boolean first, boolean active, boolean hovered) {
			if (!active) {
				return first ? button_disable_1 : button_disable_2;
			}

			if (!hovered) {
				return first ? button_idle_1 : button_idle_2;
			}

			return first ? button_hover_1 : button_hover_2;
		}

	}
}
