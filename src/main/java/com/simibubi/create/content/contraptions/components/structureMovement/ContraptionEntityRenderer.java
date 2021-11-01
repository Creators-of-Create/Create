package com.simibubi.create.content.contraptions.components.structureMovement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class ContraptionEntityRenderer<C extends AbstractContraptionEntity> extends EntityRenderer<C> {

	public ContraptionEntityRenderer(EntityRenderDispatcher manager) {
		super(manager);
	}

	@Override
	public ResourceLocation getTextureLocation(C entity) {
		return null;
	}

	@Override
	public boolean shouldRender(C entity, Frustum clippingHelper, double cameraX, double cameraY,
		double cameraZ) {
		if (entity.getContraption() == null)
			return false;
		if (!entity.isAlive())
			return false;

		return super.shouldRender(entity, clippingHelper, cameraX, cameraY, cameraZ);
	}

	@Override
	public void render(C entity, float yaw, float partialTicks, PoseStack ms, MultiBufferSource buffers,
		int overlay) {
		super.render(entity, yaw, partialTicks, ms, buffers, overlay);

		Contraption contraption = entity.getContraption();
		if (contraption != null) {
			ContraptionRenderDispatcher.renderFromEntity(entity, contraption, buffers);
		}
	}

}
