package com.simibubi.create.foundation.gui.element;

import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.utility.Color;

import net.minecraft.client.gui.GuiGraphics;

public class DelegatedStencilElement extends StencilElement {

	protected static final ElementRenderer EMPTY_RENDERER = (graphics, width, height, alpha) -> {};
	protected static final ElementRenderer DEFAULT_ELEMENT = (graphics, width, height, alpha) -> UIRenderHelper.angledGradient(graphics, 0, -3, 5, height+4, width+6, new Color(0xff_10dd10).scaleAlpha(alpha), new Color(0xff_1010dd).scaleAlpha(alpha));

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

	public <T extends DelegatedStencilElement> T withStencilRenderer(ElementRenderer renderer) {
		stencil = renderer;
		//noinspection unchecked
		return (T) this;
	}

	public <T extends DelegatedStencilElement> T withElementRenderer(ElementRenderer renderer) {
		element = renderer;
		//noinspection unchecked
		return (T) this;
	}

	@Override
	protected void renderStencil(GuiGraphics graphics) {
		stencil.render(graphics, width, height, 1);
	}

	@Override
	protected void renderElement(GuiGraphics graphics) {
		element.render(graphics, width, height, alpha);
	}

	@FunctionalInterface
	public interface ElementRenderer {
		void render(GuiGraphics graphics, int width, int height, float alpha);
	}

}
