package com.simibubi.create.content.contraptions.components.structureMovement;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

public class ContraptionEntityRenderer<C extends AbstractContraptionEntity> extends EntityRenderer<C> {

	public ContraptionEntityRenderer(EntityRendererManager manager) {
		super(manager);
	}

	@Override
	public ResourceLocation getTextureLocation(C entity) {
		return null;
	}

	@Override
	public boolean shouldRender(C entity, ClippingHelper clippingHelper, double cameraX, double cameraY,
		double cameraZ) {
		if (entity.getContraption() == null)
			return false;
		if (!entity.isAlive())
			return false;

		return super.shouldRender(entity, clippingHelper, cameraX, cameraY, cameraZ);
	}

	@Override
	public void render(C entity, float yaw, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffers,
		int overlay) {
		super.render(entity, yaw, partialTicks, ms, buffers, overlay);

		ContraptionMatrices matrices = new ContraptionMatrices(ms, entity);
		Contraption contraption = entity.getContraption();
		if (contraption != null) {
			ContraptionRenderDispatcher.render(entity, contraption, matrices, buffers);
		}
	}

}
