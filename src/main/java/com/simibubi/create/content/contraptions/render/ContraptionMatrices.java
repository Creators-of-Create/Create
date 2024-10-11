package com.simibubi.create.content.contraptions.render;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

/**
 * <p>
 * ContraptionMatrices must be cleared and setup per-contraption per-frame
 * </p>
 */
public class ContraptionMatrices {

	private final PoseStack modelViewProjection = new PoseStack();
	private final PoseStack viewProjection = new PoseStack();
	private final PoseStack model = new PoseStack();
	private final Matrix4f world = new Matrix4f();
	private final Matrix4f light = new Matrix4f();

	void setup(PoseStack viewProjection, AbstractContraptionEntity entity) {
		float partialTicks = AnimationTickHolder.getPartialTicks();

		this.viewProjection.pushPose();
		transform(this.viewProjection, viewProjection);
		model.pushPose();
		entity.applyLocalTransforms(model, partialTicks);

		modelViewProjection.pushPose();
		transform(modelViewProjection, viewProjection);
		transform(modelViewProjection, model);

		translateToEntity(world, entity, partialTicks);

		light.set(world);
		light.mul(model.last()
			.pose());
	}

	void clear() {
		clearStack(modelViewProjection);
		clearStack(viewProjection);
		clearStack(model);
		world.identity();
		light.identity();
	}

	public PoseStack getModelViewProjection() {
		return modelViewProjection;
	}

	public PoseStack getViewProjection() {
		return viewProjection;
	}

	public PoseStack getModel() {
		return model;
	}

	public Matrix4f getWorld() {
		return world;
	}

	public Matrix4f getLight() {
		return light;
	}

	public static void transform(PoseStack ms, PoseStack transform) {
		ms.last()
			.pose()
			.mul(transform.last()
				.pose());
		ms.last()
			.normal()
			.mul(transform.last()
				.normal());
	}

	public static void translateToEntity(Matrix4f matrix, Entity entity, float partialTicks) {
		double x = Mth.lerp(partialTicks, entity.xOld, entity.getX());
		double y = Mth.lerp(partialTicks, entity.yOld, entity.getY());
		double z = Mth.lerp(partialTicks, entity.zOld, entity.getZ());
		matrix.setTranslation((float) x, (float) y, (float) z);
	}

	public static void clearStack(PoseStack ms) {
		while (!ms.clear()) {
			ms.popPose();
		}
	}

}
