package com.simibubi.create.modules.curiosities.symmetry.client;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class SymmetryWandItemRenderer extends ItemStackTileEntityRenderer {

	@Override
	public void renderByItem(ItemStack stack) {

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		SymmetryWandModel mainModel = (SymmetryWandModel) itemRenderer.getModelWithOverrides(stack);
		float worldTime = AnimationTickHolder.getRenderTick();

		GlStateManager.pushMatrix();
		GlStateManager.translatef(0.5F, 0.5F, 0.5F);
		itemRenderer.renderItem(stack, mainModel.getBakedModel());

		float lastCoordx = 0;
		float lastCoordy = 0;

		GlStateManager.disableLighting();
		lastCoordx = GLX.lastBrightnessX;
		lastCoordy = GLX.lastBrightnessY;
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 240, 240);

		itemRenderer.renderItem(stack, mainModel.core);

		float floating = MathHelper.sin(worldTime) * .05f;
		GlStateManager.translated(0, floating, 0);
		float angle = worldTime * -10 % 360;
		GlStateManager.rotated(angle, 0, 1, 0);
		itemRenderer.renderItem(stack, mainModel.bits);

		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, lastCoordx, lastCoordy);
		GlStateManager.enableLighting();

		GlStateManager.popMatrix();

	}

}
