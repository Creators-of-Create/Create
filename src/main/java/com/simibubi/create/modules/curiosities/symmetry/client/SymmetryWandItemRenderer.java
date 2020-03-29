package com.simibubi.create.modules.curiosities.symmetry.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class SymmetryWandItemRenderer extends ItemStackTileEntityRenderer {

	@Override
	public void render(ItemStack stack, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		SymmetryWandModel mainModel = (SymmetryWandModel) itemRenderer.getModelWithOverrides(stack);
		float worldTime = AnimationTickHolder.getRenderTick() / 20;

		RenderSystem.pushMatrix();
		RenderSystem.translatef(0.5F, 0.5F, 0.5F);
		itemRenderer.renderItem(stack, mainModel.getBakedModel());

		float lastCoordx = 0;
		float lastCoordy = 0;

		RenderSystem.disableLighting();
		lastCoordx = GLX.lastBrightnessX;
		lastCoordy = GLX.lastBrightnessY;
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 240, 240);

		itemRenderer.renderItem(stack, mainModel.getPartial("core"));

		float floating = MathHelper.sin(worldTime) * .05f;
		RenderSystem.translated(0, floating, 0);
		float angle = worldTime * -10 % 360;
		RenderSystem.rotatef(angle, 0, 1, 0);
		itemRenderer.renderItem(stack, mainModel.getPartial("bits"));

		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, lastCoordx, lastCoordy);
		RenderSystem.enableLighting();

		RenderSystem.popMatrix();

	}

}
