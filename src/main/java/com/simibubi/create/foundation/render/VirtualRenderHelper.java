package com.simibubi.create.foundation.render;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.lib.model.ModelCache;
import com.jozufozu.flywheel.lib.model.ModelUtil;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBufferer;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBufferer.ShadeSeparatedResultConsumer;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBuilder;
import com.jozufozu.flywheel.lib.model.baked.VirtualEmptyBlockGetter;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.RenderedBuffer;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class VirtualRenderHelper {
	public static final ModelProperty<Boolean> VIRTUAL_PROPERTY = new ModelProperty<>();
	public static final ModelData VIRTUAL_DATA = ModelData.builder().with(VIRTUAL_PROPERTY, true).build();
	public static final ModelCache<BlockState> VIRTUAL_BLOCKS = new ModelCache<>(state -> new BakedModelBuilder(ModelUtil.VANILLA_RENDERER.getBlockModel(state)).modelData(VIRTUAL_DATA).build());

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
		List<MutableTemplateMesh> shadedMeshes = new ArrayList<>();
		List<MutableTemplateMesh> unshadedMeshes = new ArrayList<>();
		MutableInt totalVertexCount = new MutableInt(0);

		ShadeSeparatedResultConsumer resultConsumer = (renderType, shaded, data) -> {
			ByteBuffer vertexBuffer = data.vertexBuffer();
			int vertexCount = data.drawState().vertexCount();
			int stride = data.drawState().format().getVertexSize();
			MutableTemplateMesh mutableMesh = new MutableTemplateMesh(vertexCount);

			transferBlockVertexData(vertexBuffer, vertexCount, stride, mutableMesh, 0);

			if (shaded) {
				shadedMeshes.add(mutableMesh);
			} else {
				unshadedMeshes.add(mutableMesh);
			}
			totalVertexCount.add(vertexCount);
		};
		BakedModelBufferer.bufferSingleShadeSeparated(ModelUtil.VANILLA_RENDERER.getModelRenderer(), VirtualEmptyBlockGetter.INSTANCE, model, state, poseStack, ModelData.EMPTY, resultConsumer);

		MutableTemplateMesh mutableMesh = new MutableTemplateMesh(totalVertexCount.getValue());

		int copyIndex = 0;
		for (MutableTemplateMesh template : shadedMeshes) {
			mutableMesh.copyFrom(copyIndex, template);
			copyIndex += template.vertexCount();
		}
		int unshadedStartVertex = copyIndex;
		for (MutableTemplateMesh template : unshadedMeshes) {
			mutableMesh.copyFrom(copyIndex, template);
			copyIndex += template.vertexCount();
		}

		return new SuperByteBuffer(mutableMesh.toImmutable(), unshadedStartVertex);
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
		transferBlockVertexData(unshadedData.vertexBuffer(), unshadedData.drawState().vertexCount(), unshadedData.drawState().format().getVertexSize(), mutableMesh, unshadedStartVertex);
		return new SuperByteBuffer(mutableMesh.toImmutable(), unshadedStartVertex);
	}
}
