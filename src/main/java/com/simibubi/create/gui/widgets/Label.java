package com.simibubi.create.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class Label extends AbstractSimiWidget {

	public String text;
	protected boolean hasShadow;
	protected int color;
	protected FontRenderer font;
	
	public Label(int x, int y, String text) {
		super(x, y, Minecraft.getInstance().fontRenderer.getStringWidth(text), 10);
		font = Minecraft.getInstance().fontRenderer;
		this.text = "Label";
		color = 0xFFFFFF;
		hasShadow = false;
	}
	
	public Label colored(int color) {
		this.color = color;
		return this;
	}
	
	public Label withShadow() {
		this.hasShadow = true;
		return this;
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		if (!visible)
			return;
		
		if (hasShadow)
			font.drawStringWithShadow(text, x, y, color);
		else
			font.drawString(text, x, y, color);
	}
	
}
