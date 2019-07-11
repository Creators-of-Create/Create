package com.simibubi.create.gui.widgets;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.gui.GuiResources;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class SimiButton extends AbstractSimiWidget {	

	private GuiResources icon;
	protected boolean pressed;
	
	public SimiButton(int x, int y, GuiResources icon) {
		super(x, y, 16, 16);
		this.icon = icon;
	}
	
	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			ResourceLocation buttonTextures = GuiResources.BUTTON.location;
			ResourceLocation iconTexture = icon.location;
			this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			
			GuiResources button = 
					(pressed || !active) ? button = GuiResources.BUTTON_DOWN : 
						(isHovered) ? GuiResources.BUTTON_HOVER : 
							GuiResources.BUTTON;
			
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			Minecraft.getInstance().getTextureManager().bindTexture(buttonTextures);
			blit(x, y, button.startX, button.startY, button.width, button.height);
			Minecraft.getInstance().getTextureManager().bindTexture(iconTexture);
			blit(x +1, y +1, icon.startX, icon.startY, icon.width, icon.height);
		}
	}

	
	@Override
	public void onClick(double p_onClick_1_, double p_onClick_3_) {
		super.onClick(p_onClick_1_, p_onClick_3_);
		this.pressed = true;
	}
	
	@Override
	public void onRelease(double p_onRelease_1_, double p_onRelease_3_) {
		super.onRelease(p_onRelease_1_, p_onRelease_3_);
		this.pressed = false;
	}
	
	public void setToolTip(String text) {
		toolTip.clear();
		toolTip.add(text);
	}
	
}
