package com.simibubi.create.foundation.gui.widgets;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class Label extends AbstractSimiWidget {

	public String text;
	public String suffix;
	protected boolean hasShadow;
	protected int color;
	protected FontRenderer font;

	public Label(int x, int y, String text) {
		super(x, y, Minecraft.getInstance().fontRenderer.getStringWidth(text), 10);
		font = Minecraft.getInstance().fontRenderer;
		this.text = "Label";
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

	public void setTextAndTrim(String newText, boolean trimFront, int maxWidthPx) {
		FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
		
		if (fontRenderer.getStringWidth(newText) <= maxWidthPx) {
			text = newText;
			return;
		}
		
		String trim = "...";
		int trimWidth = fontRenderer.getStringWidth(trim);

		StringBuilder builder = new StringBuilder(newText);
		int startIndex = trimFront ? 0 : newText.length() - 1;
		int endIndex = !trimFront ? 0 : newText.length() - 1;
		int step = (int) Math.signum(endIndex - startIndex);

		for (int i = startIndex; i != endIndex; i += step) {
			String sub = builder.substring(trimFront ? i : startIndex, trimFront ? endIndex + 1 : i + 1);
			if (fontRenderer.getStringWidth(sub) + trimWidth <= maxWidthPx) {
				text = trimFront ? trim + sub : sub + trim;
				return;
			}
		}

	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		if (!visible)
			return;
		if (text == null || text.isEmpty())
			return;

		GlStateManager.color4f(1, 1, 1, 1);
		if (hasShadow)
			font.drawStringWithShadow(text + suffix, x, y, color);
		else
			font.drawString(text + suffix, x, y, color);
	}

}
