package com.simibubi.create.foundation.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

public class DelegatedStencilElement extends StencilElement {

	protected static final ElementRenderer EMPTY_RENDERER = (ms, width, height) -> {};
	protected static final ElementRenderer DEFAULT_ELEMENT = (ms, width, height) -> UIRenderHelper.angledGradient(ms, 0, -3, 5, height+4, width+6, 0xff_10dd10, 0xff_1010dd);


	protected ElementRenderer stencil;
	protected ElementRenderer element;

	public DelegatedStencilElement() {
		stencil = EMPTY_RENDERER;
		element = DEFAULT_ELEMENT;
	}

	public DelegatedStencilElement(ElementRenderer stencil, ElementRenderer element) {
		this.stencil = stencil;
		this.element = element;
	}

	public DelegatedStencilElement withStencilRenderer(ElementRenderer renderer) {
		stencil = renderer;
		return this;
	}

	public DelegatedStencilElement withElementRenderer(ElementRenderer renderer) {
		element = renderer;
		return this;
	}

	@Override
	protected void renderStencil(MatrixStack ms) {
		stencil.render(ms, width, height);
	}

	@Override
	protected void renderElement(MatrixStack ms) {
		element.render(ms, width, height);
	}

	@FunctionalInterface
	public interface ElementRenderer {
		void render(MatrixStack ms, int width, int height);
	}

}
