package com.simibubi.create.foundation.render;

import java.nio.ByteBuffer;

import org.jetbrains.annotations.Nullable;

import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.model.ModelCache;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import dev.engine_room.flywheel.lib.model.baked.ForgeBakedModelBuilder;
import dev.engine_room.flywheel.lib.model.baked.VirtualEmptyBlockGetter;
import com.mojang.blaze3d.vertex.PoseStack;

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

	private static final ModelCache<BlockState> VIRTUAL_BLOCKS = new ModelCache<>(state -> new ForgeBakedModelBuilder(ModelUtil.VANILLA_RENDERER.getBlockModel(state)).modelData(VIRTUAL_DATA).build());
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	public static boolean isVirtual(ModelData data) {
		return data.has(VirtualRenderHelper.VIRTUAL_PROPERTY) && data.get(VirtualRenderHelper.VIRTUAL_PROPERTY);
	}

	/**
	 * A copy of {@link dev.engine_room.flywheel.lib.model.Models#block(BlockState)}, but with virtual model data passed in.
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

		ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
		sbbBuilder.begin();

		ModelData modelData = model.getModelData(VirtualEmptyBlockGetter.FULL_DARK, BlockPos.ZERO, state, VIRTUAL_DATA);
		poseStack.pushPose();
		renderer.tesselateBlock(VirtualEmptyBlockGetter.FULL_DARK, model, state, BlockPos.ZERO, poseStack, sbbBuilder, false, random, 42L, OverlayTexture.NO_OVERLAY, modelData, null);
		poseStack.popPose();

		return sbbBuilder.end();
	}

	public static void transferBlockVertexData(ByteBuffer vertexBuffer, int stride, int srcIndex, MutableTemplateMesh mutableMesh, int dstIndex, int vertexCount) {
		for (int i = 0; i < vertexCount; i++) {
			mutableMesh.x(dstIndex + i, vertexBuffer.getFloat(srcIndex + i * stride));
			mutableMesh.y(dstIndex + i, vertexBuffer.getFloat(srcIndex + i * stride + 4));
			mutableMesh.z(dstIndex + i, vertexBuffer.getFloat(srcIndex + i * stride + 8));
			mutableMesh.color(dstIndex + i, vertexBuffer.getInt(srcIndex + i * stride + 12));
			mutableMesh.u(dstIndex + i, vertexBuffer.getFloat(srcIndex + i * stride + 16));
			mutableMesh.v(dstIndex + i, vertexBuffer.getFloat(srcIndex + i * stride + 20));
			mutableMesh.overlay(dstIndex + i, OverlayTexture.NO_OVERLAY);
			mutableMesh.light(dstIndex + i, vertexBuffer.getInt(srcIndex + i * stride + 24));
			mutableMesh.normal(dstIndex + i, vertexBuffer.getInt(srcIndex + i * stride + 28));
		}
	}

	private static class ThreadLocalObjects {
		public final PoseStack identityPoseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();
		public final ShadedBlockSbbBuilder sbbBuilder = new ShadedBlockSbbBuilder();
	}
}
