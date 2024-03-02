package com.simibubi.create.foundation.render;

import java.nio.ByteBuffer;

import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.lib.model.ModelCache;
import com.jozufozu.flywheel.lib.model.ModelUtil;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.VirtualEmptyBlockGetter;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class VirtualRenderHelper {
	public static final ModelProperty<Boolean> VIRTUAL_PROPERTY = new ModelProperty<>();
	public static final ModelData VIRTUAL_DATA = ModelData.builder().with(VIRTUAL_PROPERTY, true).build();

	private static final ModelCache<BlockState> VIRTUAL_BLOCKS = new ModelCache<>(state -> new BakedModelBuilder(ModelUtil.VANILLA_RENDERER.getBlockModel(state)).modelData(VIRTUAL_DATA).build());
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	public static boolean isVirtual(ModelData data) {
		return data.has(VirtualRenderHelper.VIRTUAL_PROPERTY) && data.get(VirtualRenderHelper.VIRTUAL_PROPERTY);
	}

	/**
	 * A copy of {@link com.jozufozu.flywheel.lib.model.Models#block(BlockState)}, but with virtual model data passed in.
	 * @param state The block state to get the model for.
	 * @return The model for the given block state.
	 */
	public static Model blockModel(BlockState state) {
		return VIRTUAL_BLOCKS.get(state);
	}

	public static SuperByteBuffer bufferBlock(BlockState state) {
		return bufferModel(ModelUtil.VANILLA_RENDERER.getBlockModel(state), state);
	}

	public static SuperByteBuffer bufferModel(BakedModel model, BlockState state) {
		return bufferModel(model, state, null);
	}

	public static SuperByteBuffer bufferModel(BakedModel model, BlockState state, @Nullable PoseStack poseStack) {
		BlockRenderDispatcher dispatcher = ModelUtil.VANILLA_RENDERER;
		ModelBlockRenderer renderer = dispatcher.getModelRenderer();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		if (poseStack == null) {
			poseStack = objects.identityPoseStack;
		}
		RandomSource random = objects.random;

		ShadeSeparatingVertexConsumer shadeSeparatingWrapper = objects.shadeSeparatingWrapper;
		BufferBuilder shadedBuilder = objects.shadedBuilder;
		BufferBuilder unshadedBuilder = objects.unshadedBuilder;

		shadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		unshadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		shadeSeparatingWrapper.prepare(shadedBuilder, unshadedBuilder);

		ModelData modelData = model.getModelData(VirtualEmptyBlockGetter.INSTANCE, BlockPos.ZERO, state, VIRTUAL_DATA);
		poseStack.pushPose();
		renderer.tesselateBlock(VirtualEmptyBlockGetter.INSTANCE, model, state, BlockPos.ZERO, poseStack, shadeSeparatingWrapper, false, random, 42L, OverlayTexture.NO_OVERLAY, modelData, null);
		poseStack.popPose();

		shadeSeparatingWrapper.clear();
		return endAndCombine(shadedBuilder, unshadedBuilder);
	}

	public static void transferBlockVertexData(ByteBuffer vertexBuffer, int vertexCount, int stride, MutableTemplateMesh mutableMesh, int dstIndex) {
		for (int i = 0; i < vertexCount; i++) {
			mutableMesh.x(i, vertexBuffer.getFloat(i * stride));
			mutableMesh.y(i, vertexBuffer.getFloat(i * stride + 4));
			mutableMesh.z(i, vertexBuffer.getFloat(i * stride + 8));
			mutableMesh.color(i, vertexBuffer.getInt(i * stride + 12));
			mutableMesh.u(i, vertexBuffer.getFloat(i * stride + 16));
			mutableMesh.v(i, vertexBuffer.getFloat(i * stride + 20));
			mutableMesh.overlay(i, OverlayTexture.NO_OVERLAY);
			mutableMesh.light(i, vertexBuffer.getInt(i * stride + 24));
			mutableMesh.normal(i, vertexBuffer.getInt(i * stride + 28));
		}
	}

	public static SuperByteBuffer endAndCombine(BufferBuilder shadedBuilder, BufferBuilder unshadedBuilder) {
		RenderedBuffer shadedData = shadedBuilder.end();
		int totalVertexCount = shadedData.drawState().vertexCount();
		int unshadedStartVertex = totalVertexCount;
		RenderedBuffer unshadedData = unshadedBuilder.endOrDiscardIfEmpty();
		if (unshadedData != null) {
			if (shadedData.drawState().format() != unshadedData.drawState().format()) {
				throw new IllegalStateException("Buffer formats are not equal!");
			}
			totalVertexCount += unshadedData.drawState().vertexCount();
		}

		MutableTemplateMesh mutableMesh = new MutableTemplateMesh(totalVertexCount);
		transferBlockVertexData(shadedData.vertexBuffer(), shadedData.drawState().vertexCount(), shadedData.drawState().format().getVertexSize(), mutableMesh, 0);
		if (unshadedData != null) {
			transferBlockVertexData(unshadedData.vertexBuffer(), unshadedData.drawState().vertexCount(), unshadedData.drawState().format().getVertexSize(), mutableMesh, unshadedStartVertex);
		}
		return new SuperByteBuffer(mutableMesh.toImmutable(), unshadedStartVertex);
	}

	private static class ThreadLocalObjects {
		public final PoseStack identityPoseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();
		public final ShadeSeparatingVertexConsumer shadeSeparatingWrapper = new ShadeSeparatingVertexConsumer();
		public final BufferBuilder shadedBuilder = new BufferBuilder(512);
		public final BufferBuilder unshadedBuilder = new BufferBuilder(512);
	}
}
