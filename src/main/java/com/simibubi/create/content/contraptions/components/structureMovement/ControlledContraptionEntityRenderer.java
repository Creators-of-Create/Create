package com.simibubi.create.content.contraptions.components.structureMovement;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ControlledContraptionEntityRenderer extends AbstractContraptionEntityRenderer<ControlledContraptionEntity> {

	public ControlledContraptionEntityRenderer(EntityRendererManager p_i46179_1_) {
		super(p_i46179_1_);
	}

	@Override
	protected void transform(ControlledContraptionEntity entity, float partialTicks,
		MatrixStack[] matrixStacks) {
		float angle = entity.getAngle(partialTicks);
		Axis axis = entity.getRotationAxis();

		for (MatrixStack stack : matrixStacks)
			MatrixStacker.of(stack)
				.nudge(entity.getEntityId())
				.centre()
				.rotate(angle, axis)
				.unCentre();
	}

	public Vec3d getRotation(ControlledContraptionEntity entity, float partialTicks) {
		Axis axis = entity.getRotationAxis();
		if (axis == null) return Vec3d.ZERO;

		float angle = entity.getAngle(partialTicks);

		if (axis == Axis.X) return new Vec3d(angle, 0, 0);
		if (axis == Axis.Y) return new Vec3d(0, angle, 0);
		if (axis == Axis.Z) return new Vec3d(0, 0, angle);

		throw new IllegalStateException("impossible axis");
	}
}
