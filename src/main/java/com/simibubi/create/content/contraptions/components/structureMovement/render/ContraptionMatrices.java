package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

/**
 * LIFETIME: one frame
 *
 * <p>
 *     ContraptionMatrices must be re-created per-contraption per-frame
 * </p>
 */
public class ContraptionMatrices {

	/**
	 * The results from using this are undefined.
	 */
	public static final ContraptionMatrices EMPTY = new ContraptionMatrices();

	private final MatrixStack modelViewProjection;
	private final MatrixStack viewProjection;
	private final MatrixStack model;
	private final Matrix4f world;
	private final Matrix4f light;

	private ContraptionMatrices() {
		this.viewProjection = this.model = this.modelViewProjection = new MatrixStack();
		this.world = new Matrix4f();
		this.light = new Matrix4f();
	}

	public ContraptionMatrices(MatrixStack viewProjection, AbstractContraptionEntity entity) {
		this.viewProjection = copyStack(viewProjection);
		float partialTicks = AnimationTickHolder.getPartialTicks();
		this.model = creatModelMatrix(entity, partialTicks);

		world = translateTo(entity, partialTicks);

		light = getWorld().copy();
		getLight().multiply(this.getModel()
				.last().pose());

		modelViewProjection = copyStack(viewProjection);
		transform(getModelViewProjection(), this.getModel());
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

	public static Matrix4f createModelViewPartial(AbstractContraptionEntity entity, float pt, Vector3d cameraPos) {
		float x = (float) (MathHelper.lerp(pt, entity.xOld, entity.getX()) - cameraPos.x);
		float y = (float) (MathHelper.lerp(pt, entity.yOld, entity.getY()) - cameraPos.y);
		float z = (float) (MathHelper.lerp(pt, entity.zOld, entity.getZ()) - cameraPos.z);
		Matrix4f mat = Matrix4f.createTranslateMatrix(x, y, z);
		Matrix4f modelMatrix = creatModelMatrix(entity, pt).last().pose();

		mat.multiply(modelMatrix);
		return mat;
	}

	public static MatrixStack creatModelMatrix(AbstractContraptionEntity entity, float partialTicks) {
		MatrixStack model = new MatrixStack();
		entity.doLocalTransforms(partialTicks, new MatrixStack[] { model});
		return model;
	}

	public static Matrix4f translateTo(Entity entity, float partialTicks) {
		double x = MathHelper.lerp(partialTicks, entity.xOld, entity.getX());
		double y = MathHelper.lerp(partialTicks, entity.yOld, entity.getY());
		double z = MathHelper.lerp(partialTicks, entity.zOld, entity.getZ());
		return Matrix4f.createTranslateMatrix((float) x, (float) y, (float) z);
	}

	public static void transform(MatrixStack ms, MatrixStack transform) {
		ms.last().pose()
			.multiply(transform.last()
			.pose());
		ms.last().normal()
			.mul(transform.last()
			.normal());
	}

	public static MatrixStack copyStack(MatrixStack ms) {
		MatrixStack cms = new MatrixStack();

		transform(cms, ms);

		return cms;
	}
}
