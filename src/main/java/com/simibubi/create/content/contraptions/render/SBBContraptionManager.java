package com.simibubi.create.content.contraptions.render;

import com.jozufozu.flywheel.api.event.RenderStageEvent;
import com.jozufozu.flywheel.lib.model.ModelUtil;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.Contraption.RenderedBlocks;
import com.simibubi.create.foundation.render.ShadeSeparatingVertexConsumer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.render.VirtualRenderHelper;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class SBBContraptionManager extends ContraptionRenderingWorld<ContraptionRenderInfo> {
	public static final SuperByteBufferCache.Compartment<Pair<Contraption, RenderType>> CONTRAPTION = new SuperByteBufferCache.Compartment<>();
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	public SBBContraptionManager(LevelAccessor world) {
		super(world);
	}

	@Override
	public void renderLayer(RenderStageEvent event) {
		super.renderLayer(event);
//		RenderType type = event.getType();
//		VertexConsumer consumer = event.buffers.bufferSource()
//				.getBuffer(type);
//		visible.forEach(info -> renderContraptionLayerSBB(info, type, consumer));
//
//		event.buffers.bufferSource().endBatch(type);
	}

	@Override
	public boolean invalidate(Contraption contraption) {
		for (RenderType chunkBufferLayer : RenderType.chunkBufferLayers()) {
			CreateClient.BUFFER_CACHE.invalidate(CONTRAPTION, Pair.of(contraption, chunkBufferLayer));
		}
		return super.invalidate(contraption);
	}

	@Override
	protected ContraptionRenderInfo create(Contraption c) {
		VirtualRenderWorld renderWorld = ContraptionRenderDispatcher.setupRenderWorld(world, c);
		return new ContraptionRenderInfo(c, renderWorld);
	}

	private void renderContraptionLayerSBB(ContraptionRenderInfo renderInfo, RenderType layer, VertexConsumer consumer) {
		if (!renderInfo.isVisible()) return;

		SuperByteBuffer contraptionBuffer = CreateClient.BUFFER_CACHE.get(CONTRAPTION, Pair.of(renderInfo.contraption, layer), () -> buildStructureBuffer(renderInfo.renderWorld, renderInfo.contraption, layer));

		if (!contraptionBuffer.isEmpty()) {
			ContraptionMatrices matrices = renderInfo.getMatrices();

			contraptionBuffer.transform(matrices.getModel())
					.light(matrices.getWorld())
					.hybridLight()
					.renderInto(matrices.getViewProjection(), consumer);
		}
	}

	private static SuperByteBuffer buildStructureBuffer(VirtualRenderWorld renderWorld, Contraption contraption, RenderType layer) {
		BlockRenderDispatcher dispatcher = ModelUtil.VANILLA_RENDERER;
		ModelBlockRenderer renderer = dispatcher.getModelRenderer();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		PoseStack poseStack = objects.poseStack;
		RandomSource random = objects.random;
		RenderedBlocks blocks = contraption.getRenderedBlocks();

		ShadeSeparatingVertexConsumer shadeSeparatingWrapper = objects.shadeSeparatingWrapper;
		BufferBuilder shadedBuilder = objects.shadedBuilder;
		BufferBuilder unshadedBuilder = objects.unshadedBuilder;

		shadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		unshadedBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
		shadeSeparatingWrapper.prepare(shadedBuilder, unshadedBuilder);

		ModelBlockRenderer.enableCaching();
		for (BlockPos pos : blocks.positions()) {
			BlockState state = blocks.lookup().apply(pos);
			if (state.getRenderShape() == RenderShape.MODEL) {
				BakedModel model = dispatcher.getBlockModel(state);
				ModelData modelData = contraption.modelData.getOrDefault(pos, ModelData.EMPTY);
				modelData = model.getModelData(renderWorld, pos, state, modelData);
				long randomSeed = state.getSeed(pos);
				random.setSeed(randomSeed);
				if (model.getRenderTypes(state, random, modelData).contains(layer)) {
					poseStack.pushPose();
					poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
					renderer.tesselateBlock(renderWorld, model, state, pos, poseStack, shadeSeparatingWrapper, true, random, randomSeed, OverlayTexture.NO_OVERLAY, modelData, layer);
					poseStack.popPose();
				}
			}
		}
		ModelBlockRenderer.clearCache();

		shadeSeparatingWrapper.clear();
		return VirtualRenderHelper.endAndCombine(shadedBuilder, unshadedBuilder);
	}

	private static class ThreadLocalObjects {
		public final PoseStack poseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();
		public final ShadeSeparatingVertexConsumer shadeSeparatingWrapper = new ShadeSeparatingVertexConsumer();
		public final BufferBuilder shadedBuilder = new BufferBuilder(512);
		public final BufferBuilder unshadedBuilder = new BufferBuilder(512);
	}
}
