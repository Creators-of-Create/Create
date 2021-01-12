package com.simibubi.create.content.contraptions.components.structureMovement;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class OrientedContraptionEntityRenderer extends AbstractContraptionEntityRenderer<OrientedContraptionEntity> {

	public OrientedContraptionEntityRenderer(EntityRendererManager p_i46179_1_) {
		super(p_i46179_1_);
	}

	@Override
	public boolean shouldRender(OrientedContraptionEntity entity, ClippingHelperImpl p_225626_2_, double p_225626_3_,
		double p_225626_5_, double p_225626_7_) {
		if (!super.shouldRender(entity, p_225626_2_, p_225626_3_, p_225626_5_, p_225626_7_))
			return false;
		if (entity.getContraption()
			.getType() == AllContraptionTypes.MOUNTED && entity.getRidingEntity() == null)
			return false;
		return true;
	}

	@Override
	protected void transform(OrientedContraptionEntity entity, float partialTicks, MatrixStack[] matrixStacks) {
		float angleInitialYaw = entity.getInitialYaw();
		float angleYaw = entity.getYaw(partialTicks);
		float anglePitch = entity.getPitch(partialTicks);

		for (MatrixStack stack : matrixStacks)
			stack.translate(-.5f, 0, -.5f);

		Entity ridingEntity = entity.getRidingEntity();
		if (ridingEntity instanceof AbstractMinecartEntity)
			repositionOnCart(partialTicks, matrixStacks, ridingEntity);
		else if (ridingEntity instanceof AbstractContraptionEntity) {
			if (ridingEntity.getRidingEntity() instanceof AbstractMinecartEntity)
				repositionOnCart(partialTicks, matrixStacks, ridingEntity.getRidingEntity());
			else
				repositionOnContraption(entity, partialTicks, matrixStacks, ridingEntity);
		}

		for (MatrixStack stack : matrixStacks)
			MatrixStacker.of(stack)
				.nudge(entity.getEntityId())
				.centre()
				.rotateY(angleYaw)
				.rotateZ(anglePitch)
				.rotateY(angleInitialYaw)
				.unCentre();
	}

	private void repositionOnContraption(OrientedContraptionEntity entity, float partialTicks,
										 MatrixStack[] matrixStacks, Entity ridingEntity) {
		Vec3d pos = getContraptionOffset(entity, partialTicks, ridingEntity);
		for (MatrixStack stack : matrixStacks)
			stack.translate(pos.x, pos.y, pos.z);
	}

	// Minecarts do not always render at their exact location, so the contraption
	// has to adjust aswell
	private void repositionOnCart(float partialTicks, MatrixStack[] matrixStacks, Entity ridingEntity) {
		Vec3d cartPos = getCartOffset(partialTicks, ridingEntity);

		if (cartPos == Vec3d.ZERO) return;

		for (MatrixStack stack : matrixStacks)
				stack.translate(cartPos.x, cartPos.y, cartPos.z);
	}

	private Vec3d getContraptionOffset(OrientedContraptionEntity entity, float partialTicks, Entity ridingEntity) {
		AbstractContraptionEntity parent = (AbstractContraptionEntity) ridingEntity;
		Vec3d passengerPosition = parent.getPassengerPosition(entity, partialTicks);
		double x = passengerPosition.x - MathHelper.lerp(partialTicks, entity.lastTickPosX, entity.getX());
		double y = passengerPosition.y - MathHelper.lerp(partialTicks, entity.lastTickPosY, entity.getY());
		double z = passengerPosition.z - MathHelper.lerp(partialTicks, entity.lastTickPosZ, entity.getZ());

		return new Vec3d(x, y, z);
	}

	private Vec3d getCartOffset(float partialTicks, Entity ridingEntity) {
		AbstractMinecartEntity cart = (AbstractMinecartEntity) ridingEntity;
		double cartX = MathHelper.lerp(partialTicks, cart.lastTickPosX, cart.getX());
		double cartY = MathHelper.lerp(partialTicks, cart.lastTickPosY, cart.getY());
		double cartZ = MathHelper.lerp(partialTicks, cart.lastTickPosZ, cart.getZ());
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

			return new Vec3d(cartX, cartY, cartZ);
		}

		return Vec3d.ZERO;
	}

}
