package com.simibubi.create.content.contraptions.components.structureMovement;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public abstract class AbstractContraptionEntityRenderer<C extends AbstractContraptionEntity> extends EntityRenderer<C> {

	protected AbstractContraptionEntityRenderer(EntityRendererManager p_i46179_1_) {
		super(p_i46179_1_);
	}

	@Override
	public ResourceLocation getEntityTexture(C p_110775_1_) {
		return null;
	}

	protected abstract void transform(C contraptionEntity, float partialTicks, MatrixStack[] matrixStacks);

	@Override
	public boolean shouldRender(C entity, ClippingHelper p_225626_2_, double p_225626_3_, double p_225626_5_,
		double p_225626_7_) {
		if (!super.shouldRender(entity, p_225626_2_, p_225626_3_, p_225626_5_, p_225626_7_))
			return false;
		if (!entity.isAlive())
			return false;
		if (entity.getContraption() == null)
			return false;
		return true;
	}
	
	@Override
	public void render(C entity, float yaw, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffers,
		int overlay) {
		super.render(entity, yaw, partialTicks, ms, buffers, overlay);

		// Keep a copy of the transforms in order to determine correct lighting
		MatrixStack msLocal = getLocalTransform(entity);
		MatrixStack[] matrixStacks = new MatrixStack[] { ms, msLocal };

		ms.push();
		transform(entity, partialTicks, matrixStacks);
		Contraption contraption = entity.getContraption();
		if (contraption != null)
			ContraptionRenderer.render(entity.world, contraption, ms, msLocal, buffers);
		ms.pop();

	}

	protected MatrixStack getLocalTransform(AbstractContraptionEntity entity) {
		double pt = Minecraft.getInstance()
			.getRenderPartialTicks();
		MatrixStack matrixStack = new MatrixStack();
		double x = MathHelper.lerp(pt, entity.lastTickPosX, entity.getX());
		double y = MathHelper.lerp(pt, entity.lastTickPosY, entity.getY());
		double z = MathHelper.lerp(pt, entity.lastTickPosZ, entity.getZ());
		matrixStack.translate(x, y, z);
		return matrixStack;
	}

}
