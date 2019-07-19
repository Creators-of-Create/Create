package com.simibubi.create.gui;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.Create;
import com.simibubi.create.schematic.tools.Tools;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public class ToolSelectionScreen extends Screen {

	protected List<Tools> tools;
	protected Consumer<Tools> callback;
	public boolean focused;
	private float yOffset;
	protected int selection;
	
	protected int w;
	protected int h;

	public ToolSelectionScreen(List<Tools> tools, Consumer<Tools> callback) {
		super(new StringTextComponent("Tool Selection"));
		this.minecraft = Minecraft.getInstance();
		this.tools = tools;
		this.callback = callback;
		focused = false;
		yOffset = 0;
		selection = 0;
		
		callback.accept(tools.get(selection));

		w = Math.max(tools.size() * 50 + 30, 220);
		h = 30;
	}
	
	public void setSelectedElement(Tools tool) {
		if (!tools.contains(tool))
			return;
		selection = tools.indexOf(tool);
	}
	
	public void cycle(int direction) {
		selection += (direction < 0)? 1 : -1;
		selection = (selection + tools.size()) % tools.size();
	}

	private void draw(float partialTicks) {
		MainWindow mainWindow = Minecraft.getInstance().mainWindow;

		int x = (mainWindow.getScaledWidth() - w) / 2 + 15;
		int y = mainWindow.getScaledHeight() - h - 75;
		
		GlStateManager.pushMatrix();
		GlStateManager.translatef(0, -yOffset, focused? 100 : 0);
		
		ScreenResources gray = ScreenResources.GRAY;
		GlStateManager.enableBlend();
		GlStateManager.color4f(1, 1, 1, focused? 7 / 8f : 1 / 2f);

		Minecraft.getInstance().getTextureManager().bindTexture(gray.location);
		blit(x - 15, y, gray.startX, gray.startY, w, h, gray.width, gray.height);
		
		float toolTipAlpha = yOffset / 10;
		FontRenderer font = minecraft.fontRenderer;
		List<String> toolTip = tools.get(selection).getDescription();
		int stringAlphaComponent = ((int) (toolTipAlpha * 0xFF)) << 24;
		
		if (toolTipAlpha > 0.25f) {
			GlStateManager.color4f(.7f, .7f, .8f, toolTipAlpha);
			blit(x - 15, y + 33, gray.startX, gray.startY, w, h + 22, gray.width, gray.height);
			GlStateManager.color4f(1, 1, 1, 1);
			
			if (toolTip.size() > 0)
				drawString(font, toolTip.get(0), x - 10, y + 38, 0xEEEEEE + stringAlphaComponent);
			if (toolTip.size() > 1)
				drawString(font, toolTip.get(1), x - 10, y + 50, 0xCCDDFF + stringAlphaComponent);
			if (toolTip.size() > 2)
				drawString(font, toolTip.get(2), x - 10, y + 60, 0xCCDDFF + stringAlphaComponent);
			if (toolTip.size() > 3)
				drawString(font, toolTip.get(3), x - 10, y + 72, 0xCCCCDD + stringAlphaComponent);
		}
		
		GlStateManager.color4f(1, 1, 1, 1);
		if (tools.size() > 1) {
			String translationKey = Create.TOOL_MENU.getLocalizedName().toUpperCase();
			int width = minecraft.mainWindow.getScaledWidth();
			if (!focused)
				drawCenteredString(minecraft.fontRenderer, "Hold [" + translationKey + "] to focus", width/2, y - 10, 0xCCDDFF);
			else
				drawCenteredString(minecraft.fontRenderer, "[SCROLL] to Cycle", width/2, y - 10, 0xCCDDFF);
		} else {
			x += 65;
		}
		
		for (int i = 0; i < tools.size(); i++) {
			GlStateManager.pushMatrix();
			
			float alpha = focused? 1 : .2f;
			if (i == selection) {
				GlStateManager.translatef(0, -10, 0);
				drawCenteredString(minecraft.fontRenderer, tools.get(i).getDisplayName(), x + i * 50 + 24, y + 28, 0xCCDDFF);
				alpha = 1;
			}
			GlStateManager.color4f(0, 0, 0, alpha);
			tools.get(i).getIcon().draw(this, x + i * 50 + 16, y + 12);
			GlStateManager.color4f(1, 1, 1, alpha);
			tools.get(i).getIcon().draw(this, x + i * 50 + 16, y + 11);
			
			GlStateManager.popMatrix();
		}
		
		GlStateManager.popMatrix();
	}
	
	public void update() {
		if (focused) yOffset += (10 - yOffset) * .1f;
		else yOffset *= .9f;
	}
	
	public void renderPassive(float partialTicks) {
		draw(partialTicks);
	}
	
	@Override
	public void onClose() {
		callback.accept(tools.get(selection));
	}

}
