package com.simibubi.create.foundation.gui;

import javax.annotation.Nonnull;
import com.mojang.blaze3d.matrix.MatrixStack;

public class CombinedStencilElement extends StencilElement {

	private StencilElement element1;
	private StencilElement element2;
	private ElementMode mode;

	private CombinedStencilElement() {}

	public static CombinedStencilElement of(@Nonnull StencilElement element1, @Nonnull StencilElement element2) {
		return of(element1, element2, ElementMode.FIRST);
	}

	public static CombinedStencilElement of(@Nonnull StencilElement element1, @Nonnull StencilElement element2, ElementMode mode) {
		CombinedStencilElement e = new CombinedStencilElement();
		e.element1 = element1;
		e.element2 = element2;
		e.mode = mode;
		return e;
	}

	@Override
	protected void renderStencil(MatrixStack ms) {
		ms.push();
		element1.transform(ms);
		element1.renderStencil(ms);
		ms.pop();
		ms.push();
		element2.transform(ms);
		element2.renderStencil(ms);
		ms.pop();
	}

	@Override
	protected void renderElement(MatrixStack ms) {
		if (mode.rendersFirst())
			element1.renderElement(ms);

		if (mode.rendersSecond())
			element2.renderElement(ms);
	}

	public enum ElementMode {
		FIRST, SECOND, BOTH;

		boolean rendersFirst() {
			return this == FIRST || this == BOTH;
		}

		boolean rendersSecond() {
			return this == SECOND || this == BOTH;
		}
	}
}
