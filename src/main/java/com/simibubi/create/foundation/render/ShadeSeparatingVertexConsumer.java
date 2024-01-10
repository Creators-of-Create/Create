package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.model.BakedQuad;

public class ShadeSeparatingVertexConsumer implements VertexConsumer {
	protected VertexConsumer shadedConsumer;
	protected VertexConsumer unshadedConsumer;

	public void prepare(VertexConsumer shadedConsumer, VertexConsumer unshadedConsumer) {
		this.shadedConsumer = shadedConsumer;
		this.unshadedConsumer = unshadedConsumer;
	}

	public void clear() {
		shadedConsumer = null;
		unshadedConsumer = null;
	}

	@Override
	public void putBulkData(PoseStack.Pose poseEntry, BakedQuad quad, float[] colorMuls, float red, float green, float blue, int[] combinedLights, int combinedOverlay, boolean mulColor) {
		if (quad.isShade()) {
			shadedConsumer.putBulkData(poseEntry, quad, colorMuls, red, green, blue, combinedLights, combinedOverlay, mulColor);
		} else {
			unshadedConsumer.putBulkData(poseEntry, quad, colorMuls, red, green, blue, combinedLights, combinedOverlay, mulColor);
		}
	}

	@Override
	public void putBulkData(PoseStack.Pose matrixEntry, BakedQuad bakedQuad, float[] baseBrightness, float red, float green, float blue, float alpha, int[] lightmapCoords, int overlayCoords, boolean readExistingColor) {
		if (bakedQuad.isShade()) {
			shadedConsumer.putBulkData(matrixEntry, bakedQuad, baseBrightness, red, green, blue, alpha, lightmapCoords, overlayCoords, readExistingColor);
		} else {
			unshadedConsumer.putBulkData(matrixEntry, bakedQuad, baseBrightness, red, green, blue, alpha, lightmapCoords, overlayCoords, readExistingColor);
		}
	}

	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public VertexConsumer uv(float u, float v) {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public VertexConsumer uv2(int u, int v) {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public void endVertex() {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public void defaultColor(int red, int green, int blue, int alpha) {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}

	@Override
	public void unsetDefaultColor() {
		throw new UnsupportedOperationException("ShadeSeparatingVertexConsumer only supports putBulkData!");
	}
}
