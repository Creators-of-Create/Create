package com.simibubi.create.foundation.render;

import java.util.function.Supplier;

import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;

import net.minecraft.resources.Identifier;

/**
 * Temporary Create 26.2 porting bridge for fixed-function render state calls removed from RenderSystem.
 * TODO 26.2: Replace these call sites with RenderPipeline/RenderSetup state.
 */
@Deprecated(forRemoval = true)
public class LegacyRenderSystemBridge {
	public static void enableBlend() {
	}

	public static void disableBlend() {
	}

	public static void defaultBlendFunc() {
	}

	public static void blendFunc(com.simibubi.create.foundation.render.GlStateManager.SourceFactor source,
								 com.simibubi.create.foundation.render.GlStateManager.DestFactor dest) {
	}

	public static void enableDepthTest() {
	}

	public static void disableDepthTest() {
	}

	public static void enableCull() {
	}

	public static void colorMask(boolean red, boolean green, boolean blue, boolean alpha) {
	}

	public static void setShaderColor(float red, float green, float blue, float alpha) {
	}

	public static void setShaderTexture(int slot, Identifier texture) {
	}

	public static void setShader(Supplier<?> shader) {
	}

	public static void enableColorLogicOp() {
	}

	public static void disableColorLogicOp() {
	}

	public static void logicOp(com.simibubi.create.foundation.render.GlStateManager.LogicOp op) {
	}

	public static void disableTexture() {
	}

	public static void enableTexture() {
	}

	public static void disableRescaleNormal() {
	}

	public static Matrix4fStack getModelViewStack() {
		return com.mojang.blaze3d.systems.RenderSystem.getModelViewStack();
	}

	public static void applyModelViewMatrix() {
	}

	public static void multMatrix(Matrix4f matrix) {
		getModelViewStack().mul(matrix);
	}

	public static void setShaderLights(Vector3f light1, Vector3f light2) {
	}
}
