package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;

/**
 * <p>
 *     ContraptionMatrices must be cleared and setup per-contraption per-frame
 * </p>
 */
public class ContraptionMatrices {

	private final MatrixStack modelViewProjection = new MatrixStack();
	private final MatrixStack viewProjection = new MatrixStack();
	private final MatrixStack model = new MatrixStack();
	private final Matrix4f world = new Matrix4f();
	private final Matrix4f light = new Matrix4f();

	private boolean ready;

	public ContraptionMatrices() {
		world.setIdentity();
		light.setIdentity();
	}

	public void setup(MatrixStack viewProjection, AbstractContraptionEntity entity) {
		float partialTicks = AnimationTickHolder.getPartialTicks();

		this.viewProjection.pushPose();
		transform(this.viewProjection, viewProjection);
		model.pushPose();
		entity.doLocalTransforms(partialTicks, new MatrixStack[] { model });

		modelViewProjection.pushPose();
		transform(modelViewProjection, viewProjection);
		transform(modelViewProjection, model);

		translateToEntity(world, entity, partialTicks);

		light.set(world);
		light.multiply(model
				.last().pose());

		ready = true;
	}

	public void clear() {
		clearStack(modelViewProjection);
		clearStack(viewProjection);
		clearStack(model);
		world.setIdentity();
		light.setIdentity();
		ready = false;
	}

	public MatrixStack getModelViewProjection() {
		return modelViewProjection;
	}

	public MatrixStack getViewProjection() {
		return viewProjection;
	}

	public MatrixStack getModel() {
		return model;
	}

	public Matrix4f getWorld() {
		return world;
	}

	public Matrix4f getLight() {
		return light;
	}

	public boolean isReady() {
		return ready;
	}

	public static void transform(MatrixStack ms, MatrixStack transform) {
		ms.last().pose()
			.multiply(transform.last()
			.pose());
		ms.last().normal()
			.mul(transform.last()
			.normal());
	}

	public static void translateToEntity(Matrix4f matrix, Entity entity, float partialTicks) {
		double x = MathHelper.lerp(partialTicks, entity.xOld, entity.getX());
		double y = MathHelper.lerp(partialTicks, entity.yOld, entity.getY());
		double z = MathHelper.lerp(partialTicks, entity.zOld, entity.getZ());
		matrix.setTranslation((float) x, (float) y, (float) z);
	}

	public static void clearStack(MatrixStack ms) {
		while (!ms.clear()) {
			ms.popPose();
		}
	}

}
