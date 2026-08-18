package com.simibubi.create.content.schematics.client;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import dev.engine_room.flywheel.lib.math.MatrixMath;
import net.createmod.catnip.render.MutableTemplateMesh;
import net.createmod.catnip.render.ShadeSeparatingSuperByteBuffer;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.render.TemplateMesh;
import net.minecraft.client.renderer.block.model.BakedQuad;

public class FluidSbbBuilder implements VertexConsumer {
	private static final ByteBufferBuilder BYTE_BUFFER_BUILDER = new ByteBufferBuilder(512);

	private final PoseStack poseStack;
	private BufferBuilder bufferBuilder;

	private FluidSbbBuilder(PoseStack poseStack) {
		this.poseStack = poseStack;
	}

	public static FluidSbbBuilder create(PoseStack poseStack) {
		return new FluidSbbBuilder(poseStack);
	}

	public void begin() {
		bufferBuilder = new BufferBuilder(BYTE_BUFFER_BUILDER, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
	}

	public SuperByteBuffer end() {
		MeshData data = bufferBuilder.build();
		if (data == null)
			return new ShadeSeparatingSuperByteBuffer(new TemplateMesh(0));
		TemplateMesh mesh = new MutableTemplateMesh(data).toImmutable();
		data.close();
		return new ShadeSeparatingSuperByteBuffer(mesh);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float red, float green, float blue, float alpha,
							int packedLight, int packedOverlay) {
		bufferBuilder.putBulkData(pose, quad, red, green, blue, alpha, packedLight, packedOverlay);
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightnesses, float red, float green,
							float blue, float alpha, int[] lights, int overlay, boolean readExistingColor) {
		bufferBuilder.putBulkData(pose, quad, brightnesses, red, green, blue, alpha, lights, overlay,
			readExistingColor);
	}

	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		Matrix4f matrix = poseStack.last()
			.pose();
		return bufferBuilder.addVertex(MatrixMath.transformPositionX(matrix, x, y, z),
			MatrixMath.transformPositionY(matrix, x, y, z), MatrixMath.transformPositionZ(matrix, x, y, z));
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha) {
		return bufferBuilder.setColor(red, green, blue, alpha);
	}

	@Override
	public VertexConsumer setUv(float u, float v) {
		return bufferBuilder.setUv(u, v);
	}

	@Override
	public VertexConsumer setUv1(int u, int v) {
		return bufferBuilder.setUv1(u, v);
	}

	@Override
	public VertexConsumer setUv2(int u, int v) {
		return bufferBuilder.setUv2(u, v);
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z) {
		Matrix3f matrix = poseStack.last()
			.normal();
		return bufferBuilder.setNormal(MatrixMath.transformNormalX(matrix, x, y, z),
			MatrixMath.transformNormalY(matrix, x, y, z), MatrixMath.transformNormalZ(matrix, x, y, z));
	}
}
