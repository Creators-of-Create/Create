package com.simibubi.create.foundation.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.ScreenResources;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class IconButton extends AbstractSimiWidget {	

	private ScreenResources icon;
	protected boolean pressed;
	
	public IconButton(int x, int y, ScreenResources icon) {
		super(x, y, 18, 18);
		this.icon = icon;
	}
	
	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			ResourceLocation buttonTextures = ScreenResources.BUTTON.location;
			ResourceLocation iconTexture = icon.location;
			this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			
			ScreenResources button = 
					(pressed || !active) ? button = ScreenResources.BUTTON_DOWN : 
						(isHovered) ? ScreenResources.BUTTON_HOVER : 
							ScreenResources.BUTTON;
			
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
