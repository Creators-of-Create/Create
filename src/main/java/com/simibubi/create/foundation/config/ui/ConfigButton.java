package com.simibubi.create.foundation.config.ui;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.gui.CombinedStencilElement;
import com.simibubi.create.foundation.gui.StencilElement;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.gui.widgets.StencilWidget;

public class ConfigButton extends StencilWidget {

	protected int gradientColor1 = 0xff_c0c0ff, gradientColor2 = 0xff_7b7ba3;

	public static ConfigButton createFromTextElement(int x, int y, int width, int height, StencilElement text) {
		ConfigButton button = new ConfigButton(x, y, width, height);
		StencilElement box  = new StencilElement() {
			@Override
			protected void renderStencil(MatrixStack ms) {
				fill(ms, 0, 0     , width, 0      + 1, 0xff_000000);
				fill(ms, 0, height, width +1, height + 1, 0xff_000000);
				fill(ms, 0    , 0, 0     + 1, height, 0xff_000000);
				fill(ms, width, 0, width + 1, height, 0xff_000000);
			}

			@Override
			protected void renderElement(MatrixStack ms) {
				UIRenderHelper.angledGradient(ms, 0, 0, 15, 32, 201, button.gradientColor1, button.gradientColor2);
			}
		};
		button.stencilElement = CombinedStencilElement.of(box, text);
		return button;
	}

	protected ConfigButton(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public ConfigButton(int x, int y, int width, int height, StencilElement stencilElement) {
		super(x, y, width, height, stencilElement);
	}

	@Override
	public void renderButton(@Nonnull MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
		fill(ms, x, y, x + width, y + height, 0x30_ffffff);
		RenderSystem.defaultBlendFunc();
		super.renderButton(ms, mouseX, mouseY, partialTicks);
	}
}
