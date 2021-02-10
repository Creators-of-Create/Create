package com.simibubi.create.content.contraptions.components.structureMovement;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.render.FastRenderDispatcher;
import com.simibubi.create.foundation.render.gl.backend.Backend;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
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

	public MatrixStack makeTransformMatrix(C contraptionEntity, float partialTicks) {
		MatrixStack stack = getLocalTransform(contraptionEntity, partialTicks);

		transform(contraptionEntity, partialTicks, new MatrixStack[]{ stack });

		return stack;
	}

	@Override
	public boolean shouldRender(C entity, ClippingHelperImpl p_225626_2_, double p_225626_3_, double p_225626_5_,
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
		MatrixStack msLocal = getLocalTransform(entity, AnimationTickHolder.getRenderTick());
		MatrixStack[] matrixStacks = new MatrixStack[] { ms, msLocal };

		ms.push();
		transform(entity, partialTicks, matrixStacks);
		Contraption contraption = entity.getContraption();
		if (contraption != null) {
			if (!FastRenderDispatcher.available()) {
				ContraptionRenderer.render(entity.world, contraption, ms, msLocal, buffers);
			} else {
				ContraptionRenderer.renderDynamic(entity.world, contraption, ms, msLocal, buffers);
			}
		}
		ms.pop();

	}

	protected MatrixStack getLocalTransform(AbstractContraptionEntity entity, float pt) {
		MatrixStack matrixStack = new MatrixStack();
		double x = MathHelper.lerp(pt, entity.lastTickPosX, entity.getX());
		double y = MathHelper.lerp(pt, entity.lastTickPosY, entity.getY());
		double z = MathHelper.lerp(pt, entity.lastTickPosZ, entity.getZ());
		matrixStack.translate(x, y, z);
		return matrixStack;
	}

}
