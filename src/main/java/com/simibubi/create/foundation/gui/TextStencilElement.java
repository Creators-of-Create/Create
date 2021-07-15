package com.simibubi.create.foundation.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;

public class TextStencilElement extends DelegatedStencilElement {

	protected FontRenderer font;
	protected IFormattableTextComponent component;
	protected boolean centerVertically = false;
	protected boolean centerHorizontally = false;

	public TextStencilElement(FontRenderer font) {
		super();
		this.font = font;
		height = 10;
	}

	public TextStencilElement(FontRenderer font, String text) {
		this(font);
		component = new StringTextComponent(text);
	}

	public TextStencilElement(FontRenderer font, IFormattableTextComponent component) {
		this(font);
		this.component = component;
	}

	public TextStencilElement withText(String text) {
		component = new StringTextComponent(text);
		return this;
	}

	public TextStencilElement withText(IFormattableTextComponent component) {
		this.component = component;
		return this;
	}

	public TextStencilElement centered(boolean vertical, boolean horizontal) {
		this.centerVertically = vertical;
		this.centerHorizontally = horizontal;
		return this;
	}

	@Override
	protected void renderStencil(MatrixStack ms) {

		float x = 0, y = 0;
		if (centerHorizontally)
			x = width / 2f - font.width(component) / 2f;

		if (centerVertically)
			y = height / 2f - (font.lineHeight - 1) / 2f;

		font.draw(ms, component, x, y, 0xff_000000);
	}

	@Override
	protected void renderElement(MatrixStack ms) {
		float x = 0, y = 0;
		if (centerHorizontally)
			x = width / 2f - font.width(component) / 2f;

		if (centerVertically)
			y = height / 2f - (font.lineHeight - 1) / 2f;

		ms.pushPose();
		ms.translate(x, y, 0);
		element.render(ms, font.width(component), font.lineHeight + 2, alpha);
		ms.popPose();
	}

	public IFormattableTextComponent getComponent() {
		return component;
	}
}
