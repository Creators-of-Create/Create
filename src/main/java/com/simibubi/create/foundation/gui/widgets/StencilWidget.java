package com.simibubi.create.foundation.gui.widgets;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.StencilElement;

public class StencilWidget extends AbstractSimiWidget {

	protected StencilElement stencilElement;

	protected StencilWidget(int x, int y) {
		super(x, y, 42, 42);
	}

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
		stencilElement.withBounds(width, height).render(ms);

		ms.pop();
	}

	public <T extends StencilWidget> T modifyElement(Consumer<StencilElement> consumer) {
		if (stencilElement != null)
			consumer.accept(stencilElement);
		//noinspection unchecked
		return (T) this;
	}

	public <T extends StencilWidget> T mapElement(UnaryOperator<StencilElement> function) {
		if (stencilElement != null)
			stencilElement = function.apply(stencilElement);
		//noinspection unchecked
		return (T) this;
	}

	public StencilElement getStencilElement() {
		return stencilElement;
	}
}
