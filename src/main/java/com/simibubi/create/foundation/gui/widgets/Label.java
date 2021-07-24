package com.simibubi.create.foundation.gui.widgets;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class Label extends AbstractSimiWidget {

	public ITextComponent text;
	public String suffix;
	protected boolean hasShadow;
	protected int color;
	protected FontRenderer font;

	public Label(int x, int y, ITextComponent text) {
		super(x, y, Minecraft.getInstance().font.width(text), 10);
		font = Minecraft.getInstance().font;
		this.text = new StringTextComponent("Label");
		color = 0xFFFFFF;
		hasShadow = false;
		suffix = "";
	}

	public Label colored(int color) {
		this.color = color;
		return this;
	}

	public Label withShadow() {
		this.hasShadow = true;
		return this;
	}

	public Label withSuffix(String s) {
		suffix = s;
		return this;
	}

	public void setTextAndTrim(ITextComponent newText, boolean trimFront, int maxWidthPx) {
		FontRenderer fontRenderer = Minecraft.getInstance().font;
		
		if (fontRenderer.width(newText) <= maxWidthPx) {
			text = newText;
			return;
		}
		
		String trim = "...";
		int trimWidth = fontRenderer.width(trim);

		String raw = newText.getString();
		StringBuilder builder = new StringBuilder(raw);
		int startIndex = trimFront ? 0 : raw.length() - 1;
		int endIndex = !trimFront ? 0 : raw.length() - 1;
		int step = (int) Math.signum(endIndex - startIndex);

		for (int i = startIndex; i != endIndex; i += step) {
			String sub = builder.substring(trimFront ? i : startIndex, trimFront ? endIndex + 1 : i + 1);
			if (fontRenderer.width(new StringTextComponent(sub).setStyle(newText.getStyle())) + trimWidth <= maxWidthPx) {
				text = new StringTextComponent(trimFront ? trim + sub : sub + trim).setStyle(newText.getStyle());
				return;
			}
		}

	}

	@Override
	public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		if (!visible)
			return;
		if (text == null || text.getString().isEmpty())
			return;

		RenderSystem.color4f(1, 1, 1, 1);
		IFormattableTextComponent copy = text.plainCopy();
		if (suffix != null && !suffix.isEmpty())
			copy.append(suffix);
		
		if (hasShadow)
			font.drawShadow(matrixStack, copy, x, y, color);
		else
			font.draw(matrixStack, copy, x, y, color);
	}

}
