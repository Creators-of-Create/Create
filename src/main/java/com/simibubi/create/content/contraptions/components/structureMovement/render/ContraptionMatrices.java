package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;

public class ContraptionMatrices {

	/**
	 * The results from using this are undefined.
	 */
	public static final ContraptionMatrices EMPTY = new ContraptionMatrices();

	public final MatrixStack entityStack;
	public final MatrixStack contraptionStack;
	public final MatrixStack finalStack;
	public final Matrix4f entityMatrix;
	public final Matrix4f lightMatrix;

	private ContraptionMatrices() {
		this.entityStack = this.contraptionStack = this.finalStack = new MatrixStack();
		this.entityMatrix = new Matrix4f();
		this.lightMatrix = new Matrix4f();
	}

	public ContraptionMatrices(MatrixStack entityStack, AbstractContraptionEntity entity) {
		this.entityStack = copyStack(entityStack);
		this.contraptionStack = new MatrixStack();
		float partialTicks = AnimationTickHolder.getPartialTicks();
		entity.doLocalTransforms(partialTicks, new MatrixStack[] { this.contraptionStack });

		entityMatrix = translateTo(entity, partialTicks);

		lightMatrix = entityMatrix.copy();
		lightMatrix.multiply(contraptionStack.last().pose());

		finalStack = copyStack(entityStack);
		transform(finalStack, contraptionStack);
	}

	public MatrixStack getFinalStack() {
		return finalStack;
	}

	public Matrix4f getFinalModel() {
		return finalStack.last().pose();
	}

	public Matrix3f getFinalNormal() {
		return finalStack.last().normal();
	}

	public Matrix4f getFinalLight() {
		return lightMatrix;
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

	public Matrix4f contraptionPose() {
		return contraptionStack.last()
				.pose();
	}
}
