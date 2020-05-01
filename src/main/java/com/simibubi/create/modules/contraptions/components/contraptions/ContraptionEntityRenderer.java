package com.simibubi.create.modules.contraptions.components.contraptions;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ContraptionEntityRenderer extends EntityRenderer<ContraptionEntity> {

	public ContraptionEntityRenderer(EntityRendererManager rendererManager) {
		super(rendererManager);
	}

	@Override
	public ResourceLocation getEntityTexture(ContraptionEntity arg0) {
		return null;
	}

	@Override
	public void render(ContraptionEntity entity, float yaw, float partialTicks, MatrixStack ms,
			IRenderTypeBuffer buffers, int overlay) {
		if (!entity.isAlive())
			return;
		if (entity.getContraption() == null)
			return;
		if (entity.getContraption().getType() == AllContraptionTypes.MOUNTED && entity.getRidingEntity() == null)
			return;

		// TODO 1.15 buffered render
		RenderSystem.pushMatrix();
		long randomBits = (long) entity.getEntityId() * 493286711L;
		randomBits = randomBits * randomBits * 4392167121L + randomBits * 98761L;
		float xNudge = (((float) (randomBits >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float yNudge = (((float) (randomBits >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float zNudge = (((float) (randomBits >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		RenderSystem.translatef(xNudge, yNudge, zNudge);

		float degYaw = entity.getYaw(partialTicks);
		float degPitch = entity.getPitch(partialTicks);
		float degRoll = entity.getRoll(partialTicks);

		float angleYaw = (float) (degYaw / 180 * Math.PI);
		float anglePitch = (float) (degPitch / 180 * Math.PI);
		float angleRoll = (float) (degRoll / 180 * Math.PI);

		Entity ridingEntity = entity.getRidingEntity();
		if (ridingEntity != null && ridingEntity instanceof AbstractMinecartEntity) {
			AbstractMinecartEntity cart = (AbstractMinecartEntity) ridingEntity;

			double cartX = MathHelper.lerp((double) partialTicks, cart.lastTickPosX, cart.getX());
			double cartY = MathHelper.lerp((double) partialTicks, cart.lastTickPosY, cart.getY());
			double cartZ = MathHelper.lerp((double) partialTicks, cart.lastTickPosZ, cart.getZ());
			Vec3d cartPos = cart.getPos(cartX, cartY, cartZ);

			if (cartPos != null) {
				Vec3d cartPosFront = cart.getPosOffset(cartX, cartY, cartZ, (double) 0.3F);
				Vec3d cartPosBack = cart.getPosOffset(cartX, cartY, cartZ, (double) -0.3F);
				if (cartPosFront == null)
					cartPosFront = cartPos;
				if (cartPosBack == null)
					cartPosBack = cartPos;

				cartX = cartPos.x - cartX;
				cartY = (cartPosFront.y + cartPosBack.y) / 2.0D - cartY;
				cartZ = cartPos.z - cartZ;

				RenderSystem.translatef((float) cartX, (float) cartY, (float) cartZ);
			}
			GlStateManager.translatef(-.5f, 0, -.5f);
		}

		Vec3d rotationOffset = VecHelper.getCenterOf(BlockPos.ZERO);
		TessellatorHelper.prepareFastRender();
		RenderSystem.enableCull();
		TessellatorHelper.begin(DefaultVertexFormats.BLOCK);
		ContraptionRenderer.render(entity.world, entity.getContraption(), superByteBuffer -> {
			superByteBuffer.translate(-rotationOffset.x, -rotationOffset.y, -rotationOffset.z);
			superByteBuffer.rotate(Axis.X, angleRoll);
			superByteBuffer.rotate(Axis.Y, angleYaw);
			superByteBuffer.rotate(Axis.Z, anglePitch);
			superByteBuffer.translate(rotationOffset.x, rotationOffset.y, rotationOffset.z);
		}, ms, Tessellator.getInstance().getBuffer());
		TessellatorHelper.draw();

		if (!entity.getContraption().customRenderTEs.isEmpty()) {
			RenderSystem.pushMatrix();
			RenderSystem.translated(rotationOffset.x, rotationOffset.y, rotationOffset.z);
			RenderSystem.rotatef(degPitch, 0, 0, 1);
			RenderSystem.rotatef(degYaw, 0, 1, 0);
			RenderSystem.rotatef(degRoll, 1, 0, 0);
			RenderSystem.translated(-rotationOffset.x, -rotationOffset.y, -rotationOffset.z);
			ContraptionRenderer.renderTEsWithGL(entity.world, entity.getContraption(), entity.getPositionVec(),
					new Vec3d(degRoll, degYaw, degPitch), ms, buffers);
			RenderSystem.popMatrix();
		}

		RenderSystem.disableCull();
		RenderSystem.popMatrix();
		RenderSystem.shadeModel(7424);
		RenderSystem.alphaFunc(516, 0.1F);
		RenderSystem.matrixMode(5888);
		RenderHelper.enable();

		super.render(entity, yaw, partialTicks, ms, buffers, overlay);
	}

}
