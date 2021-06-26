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
		Matrix4f finalModel = entityStack.peek().getModel().copy();
		finalModel.multiply(contraptionStack.peek().getModel());
		return finalModel;
	}

	public Matrix3f getFinalNormal() {
		Matrix3f finalNormal = entityStack.peek().getNormal().copy();
		finalNormal.multiply(contraptionStack.peek().getNormal());
		return finalNormal;
	}

	public Matrix4f getFinalLight() {
		Matrix4f lightTransform = entityMatrix.copy();
		lightTransform.multiply(contraptionStack.peek().getModel());
		return lightTransform;
	}

	public static Matrix4f translateTo(Entity entity, float partialTicks) {
		double x = MathHelper.lerp(partialTicks, entity.lastTickPosX, entity.getX());
		double y = MathHelper.lerp(partialTicks, entity.lastTickPosY, entity.getY());
		double z = MathHelper.lerp(partialTicks, entity.lastTickPosZ, entity.getZ());
		return Matrix4f.translate((float) x, (float) y, (float) z);
	}

	public static void transform(MatrixStack ms, MatrixStack transform) {
		ms.peek().getModel()
			.multiply(transform.peek()
			.getModel());
		ms.peek().getNormal()
			.multiply(transform.peek()
			.getNormal());
	}
}
