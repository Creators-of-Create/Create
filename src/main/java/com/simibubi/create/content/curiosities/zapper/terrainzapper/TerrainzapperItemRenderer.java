package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.curiosities.zapper.ZapperItemRenderer;
import com.simibubi.create.content.curiosities.zapper.ZapperRenderHandler;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;

@SuppressWarnings("deprecation")
public class TerrainzapperItemRenderer extends ZapperItemRenderer {

	@Override
	public void render(ItemStack stack, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {
		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		TerrainzapperModel mainModel = (TerrainzapperModel) itemRenderer.getItemModelWithOverrides(stack, Minecraft.getInstance().world, null);
		float pt = Minecraft.getInstance().getRenderPartialTicks();
		float worldTime = AnimationTickHolder.getRenderTick() / 20;

		ms.push();
		ms.translate(0.5F, 0.5F, 0.5F);
		int lastBl = LightTexture.getBlockLightCoordinates(light);
		int lastSl = LightTexture.getSkyLightCoordinates(light);
		itemRenderer.renderItem(stack, TransformType.NONE, false, ms, buffer,
				LightTexture.pack(Math.min(lastBl + 4, 15), Math.min(lastSl + 7, 15)), overlay,
				mainModel.getBakedModel());

		// Block indicator
		if (mainModel.getCurrentPerspective() == TransformType.GUI && stack.hasTag()
				&& stack.getTag().contains("BlockUsed"))
			renderBlockUsed(stack, itemRenderer, ms, buffer, light, overlay);

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
		float multiplier = MathHelper.sin(worldTime * 5);
		if (mainHand || offHand) {
			multiplier = animation;
		}
		
		itemRenderer.renderItem(stack, TransformType.NONE, false, ms, buffer,
				LightTexture.pack((int) (15 * multiplier), 7), overlay, mainModel.getPartial("terrain_core"));

		// Accelerator spins
		float angle = worldTime * -25;
		if (mainHand || offHand)
			angle += 360 * animation;

		angle %= 360;
		float offset = -.155f;
		ms.translate(0, offset, 0);
		ms.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(angle));
		ms.translate(0, -offset, 0);
		itemRenderer.renderItem(stack, TransformType.NONE, false, ms, buffer, light, overlay,
				mainModel.getPartial("terrain_accelerator"));

		ms.pop();
	}

}
