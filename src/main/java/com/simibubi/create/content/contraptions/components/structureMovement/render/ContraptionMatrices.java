package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;

public class ContraptionMatrices {
	public final MatrixStack entityStack;
	public final MatrixStack contraptionStack;
	public final Matrix4f entityMatrix;

	public ContraptionMatrices(MatrixStack entityStack, AbstractContraptionEntity entity) {
		this.entityStack = entityStack;
		this.contraptionStack = new MatrixStack();
		float partialTicks = AnimationTickHolder.getPartialTicks();
		entity.doLocalTransforms(partialTicks, new MatrixStack[] { this.contraptionStack });
		entityMatrix = translateTo(entity, partialTicks);
	}

	public MatrixStack getFinalStack() {
		MatrixStack finalStack = new MatrixStack();
		transform(finalStack, entityStack);
		transform(finalStack, contraptionStack);
		return finalStack;
	}

	public Matrix4f getFinalModel() {
		Matrix4f finalModel = entityStack.last().pose().copy();
		finalModel.multiply(contraptionStack.last().pose());
		return finalModel;
	}

	public Matrix3f getFinalNormal() {
		Matrix3f finalNormal = entityStack.last().normal().copy();
		finalNormal.mul(contraptionStack.last().normal());
		return finalNormal;
	}

	public Matrix4f getFinalLight() {
		Matrix4f lightTransform = entityMatrix.copy();
		lightTransform.multiply(contraptionStack.last().pose());
		return lightTransform;
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
}
