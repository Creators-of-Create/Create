package com.simibubi.create.foundation.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

import com.mojang.blaze3d.matrix.MatrixStack;

public class TextStencilElement extends StencilElement {

	protected static final ElementRenderer DEFAULT_RENDERER = ((ms, width, _height) -> UIRenderHelper.angledGradient(ms, 15, -3, 5, 14, width+6, 0xff_10dd10, 0x1010dd));

	protected FontRenderer font;
	protected IFormattableTextComponent component;
	protected ElementRenderer elementRenderer = DEFAULT_RENDERER;

	public TextStencilElement(FontRenderer font) {
		super();
		this.font = font;
	}

	public TextStencilElement(FontRenderer font, String text) {
		this(font);
		component = new StringTextComponent(text);
	}

	public TextStencilElement(FontRenderer font, IFormattableTextComponent component) {
		this(font);
		this.component = component;
	}

	public TextStencilElement withElementRenderer(ElementRenderer renderer) {
		elementRenderer = renderer;
		return this;
	}

	public TextStencilElement withText(String text) {
		component = new StringTextComponent(text);
		return this;
	}

	public TextStencilElement withText(IFormattableTextComponent component) {
		this.component = component;
		return this;
	}

	@Override
	protected void renderStencil(MatrixStack ms) {
		font.draw(ms, component, 0, 0, 0xff_000000);
	}

	@Override
	protected void renderElement(MatrixStack ms) {
		elementRenderer.render(ms, font.getWidth(component), 10);
	}

	@FunctionalInterface
	public interface ElementRenderer {
		void render(MatrixStack ms, int width, int height);
	}

	public static class Centered extends TextStencilElement {

		int width;

		public Centered(FontRenderer font, String text, int width) {
			super(font, text);
			this.width = width;
		}

		public Centered(FontRenderer font, IFormattableTextComponent component, int width) {
			super(font, component);
			this.width = width;
		}

		@Override
		protected void renderStencil(MatrixStack ms) {
			int textWidth = font.getWidth(component);
			font.draw(ms, component, width / 2f - textWidth / 2f, 0, 0xff_000000);
		}

		@Override
		protected void renderElement(MatrixStack ms) {
			elementRenderer.render(ms, width, 10);
		}
	}
}
