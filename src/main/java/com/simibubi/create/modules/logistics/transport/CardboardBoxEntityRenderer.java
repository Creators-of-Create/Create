package com.simibubi.create.modules.logistics.transport;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

public class CardboardBoxEntityRenderer extends EntityRenderer<CardboardBoxEntity> {

	public CardboardBoxEntityRenderer(EntityRendererManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(CardboardBoxEntity entity) {
		return null;
	}

	@Override
	public boolean shouldRender(CardboardBoxEntity livingEntity, ICamera camera, double camX, double camY,
			double camZ) {
		return super.shouldRender(livingEntity, camera, camX, camY, camZ);
	}

	@Override
	public void renderMultipass(CardboardBoxEntity entityIn, double x, double y, double z, float entityYaw,
			float partialTicks) {
		super.renderMultipass(entityIn, x, y, z, entityYaw, partialTicks);
	}

	@Override
	public void doRender(CardboardBoxEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
		IBakedModel model = getModelForBox(entity);
		if (model == null)
			return;

		bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);

		if (entity.extractorSide != null && entity.extractorAnimationProgress > 0) {
			float offset = 0;
			float yOffset = 0.55f;
			float time = entity.extractorAnimationProgress - partialTicks;
			float scale = 1;
			if (time > 5) {
				float step = (time - 10) / 10;
				offset = MathHelper.lerp(step, -.5f, -1.5f);
				scale = MathHelper.lerp(step, .3f, .3f);
			} else {
				float step = time / 5;
				float cubicStep = step * step * step;
				offset = -cubicStep * .5f;
				yOffset = step * .55f;
				scale = MathHelper.lerp(cubicStep, 1, .3f);
			}
			
			GlStateManager.scaled(scale, scale, scale);
			Vec3i vec = entity.extractorSide.getDirectionVec();
			GlStateManager.translated(offset * vec.getX(), offset * vec.getY() + yOffset, offset * vec.getZ());
		}

		GlStateManager.rotated(entity.rotationYaw, 0, 1, 0);
		GlStateManager.translated(-.5, 0, .5);

		Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightness(model,
				Blocks.AIR.getDefaultState(), 1, false);
		GlStateManager.popMatrix();

		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	public IBakedModel getModelForBox(CardboardBoxEntity entity) {
		if (entity.getBox() == null || entity.getBox().isEmpty())
			return null;
		return Minecraft.getInstance().getItemRenderer().getModelWithOverrides(entity.getBox());
	}

}
