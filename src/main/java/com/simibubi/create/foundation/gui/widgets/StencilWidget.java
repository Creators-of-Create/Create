package com.simibubi.create.foundation.gui.widgets;

import javax.annotation.Nonnull;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.StencilElement;

public class StencilWidget extends AbstractSimiWidget {

	protected StencilElement stencilElement;

	protected StencilWidget(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public StencilWidget(int x, int y, int width, int height, StencilElement stencilElement) {
		super(x, y, width, height);
		this.stencilElement = stencilElement;
	}

	@Override
	public void renderButton(@Nonnull MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		ms.push();

		ms.translate(x, y, 0);
		stencilElement.render(ms);

		ms.pop();
	}

	public StencilElement getStencilElement() {
		return stencilElement;
	}
}
