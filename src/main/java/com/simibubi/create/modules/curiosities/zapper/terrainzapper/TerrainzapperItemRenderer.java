package com.simibubi.create.modules.curiosities.zapper.terrainzapper;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.modules.curiosities.zapper.ZapperItemRenderer;
import com.simibubi.create.modules.curiosities.zapper.ZapperRenderHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings("deprecation")
public class TerrainzapperItemRenderer extends ZapperItemRenderer {

	@Override
	public void renderByItem(ItemStack stack) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		TerrainzapperModel mainModel = (TerrainzapperModel) itemRenderer.getModelWithOverrides(stack);
		float pt = Minecraft.getInstance().getRenderPartialTicks();
		float worldTime = AnimationTickHolder.getRenderTick() / 20;

		GlStateManager.pushMatrix();
		GlStateManager.translatef(0.5F, 0.5F, 0.5F);
		float lastCoordx = GLX.lastBrightnessX;
		float lastCoordy = GLX.lastBrightnessY;
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, Math.min(lastCoordx + 60, 240), Math.min(lastCoordy + 120, 240));
		itemRenderer.renderItem(stack, mainModel.getBakedModel());

		// Block indicator
		if (mainModel.getCurrentPerspective() == TransformType.GUI && stack.hasTag()
				&& stack.getTag().contains("BlockUsed"))
			renderBlockUsed(stack, itemRenderer);

		ClientPlayerEntity player = Minecraft.getInstance().player;
		boolean leftHanded = player.getPrimaryHand() == HandSide.LEFT;
		boolean mainHand = player.getHeldItemMainhand() == stack;
		boolean offHand = player.getHeldItemOffhand() == stack;
		float last = mainHand ^ leftHanded ? ZapperRenderHandler.lastRightHandAnimation
				: ZapperRenderHandler.lastLeftHandAnimation;
		float current = mainHand ^ leftHanded ? ZapperRenderHandler.rightHandAnimation
				: ZapperRenderHandler.leftHandAnimation;
		float animation = MathHelper.clamp(MathHelper.lerp(pt, last, current) * 5, 0, 1);

		// Core glows
		GlStateManager.disableLighting();
		float multiplier = MathHelper.sin(worldTime * 5);
		if (mainHand || offHand) {
			multiplier = animation;
		}
		
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, multiplier * 240, 120);
		itemRenderer.renderItem(stack, mainModel.getPartial("terrain_core"));
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, lastCoordx, lastCoordy);
		GlStateManager.enableLighting();

		// Accelerator spins
		float angle = worldTime * -25;
		if (mainHand || offHand)
			angle += 360 * animation;

		angle %= 360;
		float offset = -.155f;
		GlStateManager.translatef(0, offset, 0);
		GlStateManager.rotatef(angle, 0, 0, 1);
		GlStateManager.translatef(0, -offset, 0);
		itemRenderer.renderItem(stack, mainModel.getPartial("terrain_accelerator"));

		GlStateManager.popMatrix();
	}

}
