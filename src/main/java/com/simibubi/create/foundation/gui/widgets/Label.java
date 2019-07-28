package com.simibubi.create.foundation.gui.widgets;

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
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		if (!visible)
			return;
		if (text == null || text.isEmpty())
			return;
		
		if (hasShadow)
			font.drawStringWithShadow(text + suffix, x, y, color);
		else
			font.drawString(text + suffix, x, y, color);
	}
	
}
