package com.simibubi.create.modules.contraptions;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;

public class WrenchItemRenderer extends ItemStackTileEntityRenderer {

	@Override
	public void renderByItem(ItemStack stack) {

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		WrenchModel mainModel = (WrenchModel) itemRenderer.getModelWithOverrides(stack);
		float worldTime = AnimationTickHolder.getRenderTick();

		GlStateManager.pushMatrix();
		GlStateManager.translatef(0.5F, 0.5F, 0.5F);
		itemRenderer.renderItem(stack, mainModel.getBakedModel());

		float angle = worldTime * -10 % 360;
		
		float xOffset = -1/32f;
		float zOffset = 0;
		GlStateManager.translatef(-xOffset, 0, -zOffset);
		GlStateManager.rotated(angle, 0, 1, 0);
		GlStateManager.translatef(xOffset, 0, zOffset);
		
		itemRenderer.renderItem(stack, mainModel.gear);

		GlStateManager.popMatrix();
	}

}
