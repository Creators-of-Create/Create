package com.simibubi.create.content.contraptions.components.structureMovement;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.util.ResourceLocation;
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
		if (entity.getContraption()
			.getType() == AllContraptionTypes.MOUNTED && entity.getRidingEntity() == null)
			return;

		// Keep a copy of the transforms in order to determine correct lighting
		MatrixStack msLocal = getLocalTransform(entity);
		MatrixStack[] matrixStacks = new MatrixStack[] { ms, msLocal };

		float degYaw = entity.getYaw(partialTicks);
		float degPitch = entity.getPitch(partialTicks);
		float degRoll = entity.getRoll(partialTicks);

		float angleYaw = (float) (degYaw / 180 * Math.PI);
		float anglePitch = (float) (degPitch / 180 * Math.PI);
		float angleRoll = (float) (degRoll / 180 * Math.PI);

		ms.push();
		Entity ridingEntity = entity.getRidingEntity();
		if (ridingEntity != null && ridingEntity instanceof AbstractMinecartEntity) {
			AbstractMinecartEntity cart = (AbstractMinecartEntity) ridingEntity;
			double cartX = MathHelper.lerp(partialTicks, cart.lastTickPosX, cart.getX());
			double cartY = MathHelper.lerp(partialTicks, cart.lastTickPosY, cart.getY());
			double cartZ = MathHelper.lerp(partialTicks, cart.lastTickPosZ, cart.getZ());
			Vec3d cartPos = cart.getPos(cartX, cartY, cartZ);

			for (MatrixStack stack : matrixStacks)
				stack.translate(-.5f, 0, -.5f);

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

				for (MatrixStack stack : matrixStacks)
					stack.translate(cartX, cartY, cartZ);
			}
		}

		for (MatrixStack stack : matrixStacks)
			MatrixStacker.of(stack)
				.nudge(entity.getEntityId())
				.centre()
				.rotateRadians(angleRoll, angleYaw, anglePitch)
				.unCentre();
		ContraptionRenderer.render(entity.world, entity.getContraption(), ms, msLocal, buffers);
		ms.pop();

		super.render(entity, yaw, partialTicks, ms, buffers, overlay);
	}

	protected MatrixStack getLocalTransform(ContraptionEntity entity) {
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
