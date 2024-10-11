package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.simibubi.create.foundation.mixin.accessor.BufferBuilderAccessor;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.renderer.block.model.BakedQuad;

public class ShadedBlockSbbBuilder implements VertexConsumer {
	protected final BufferBuilder bufferBuilder;
	protected final IntList shadeSwapVertices = new IntArrayList();
	protected boolean currentShade;

	public ShadedBlockSbbBuilder(BufferBuilder bufferBuilder) {
		this.bufferBuilder = bufferBuilder;
	}

	public ShadedBlockSbbBuilder() {
		this(new BufferBuilder(512));
	}

	public void begin() {
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		shadeSwapVertices.clear();
		currentShade = true;
	}

	public SuperByteBuffer end() {
		RenderedBuffer data = bufferBuilder.end();
		int vertexCount = data.drawState().vertexCount();
		MutableTemplateMesh mutableMesh = new MutableTemplateMesh(vertexCount);
		VirtualRenderHelper.transferBlockVertexData(data.vertexBuffer(), data.drawState().format().getVertexSize(), 0, mutableMesh, 0, vertexCount);
		return new SuperByteBuffer(mutableMesh.toImmutable(), shadeSwapVertices.toIntArray());
	}

	public BufferBuilder unwrap(boolean shade) {
		prepareForGeometry(shade);
		return bufferBuilder;
	}

	private void prepareForGeometry(boolean shade) {
		if (shade != currentShade) {
			shadeSwapVertices.add(((BufferBuilderAccessor) bufferBuilder).create$getVertices());
			currentShade = shade;
		}
	}

	private void prepareForGeometry(BakedQuad quad) {
		prepareForGeometry(quad.isShade());
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, int light, int overlay) {
		prepareForGeometry(quad);
		bufferBuilder.putBulkData(pose, quad, red, green, blue, light, overlay);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha, int light, int overlay, boolean readExistingColor) {
		prepareForGeometry(quad);
		bufferBuilder.putBulkData(pose, quad, red, green, blue, alpha, light, overlay, readExistingColor);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightnesses, float red, float green, float blue, int[] lights, int overlay, boolean readExistingColor) {
		prepareForGeometry(quad);
		bufferBuilder.putBulkData(pose, quad, brightnesses, red, green, blue, lights, overlay, readExistingColor);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightnesses, float red, float green, float blue, float alpha, int[] lights, int overlay, boolean readExistingColor) {
		prepareForGeometry(quad);
		bufferBuilder.putBulkData(pose, quad, brightnesses, red, green, blue, alpha, lights, overlay, readExistingColor);
	}

	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}

	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}

	@Override
	public VertexConsumer uv(float u, float v) {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}

	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}

	@Override
	public VertexConsumer uv2(int u, int v) {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}

	@Override
	public void endVertex() {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}

	@Override
	public void defaultColor(int red, int green, int blue, int alpha) {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}

	@Override
	public void unsetDefaultColor() {
		throw new UnsupportedOperationException("ShadedBlockSbbBuilder only supports putBulkData!");
	}
}
