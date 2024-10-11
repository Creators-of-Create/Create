package com.simibubi.create.content.contraptions.render;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.Contraption.RenderedBlocks;
import com.simibubi.create.content.contraptions.ContraptionWorld;
import com.simibubi.create.foundation.render.ShadedBlockSbbBuilder;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.ModelUtil;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.client.model.data.ModelData;

public class ContraptionRenderInfo {
	public static final SuperByteBufferCache.Compartment<Pair<Contraption, RenderType>> CONTRAPTION = new SuperByteBufferCache.Compartment<>();
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private final Contraption contraption;
	private final VirtualRenderWorld renderWorld;
	private final ContraptionMatrices matrices = new ContraptionMatrices();

	ContraptionRenderInfo(Level level, Contraption contraption) {
		this.contraption = contraption;
		this.renderWorld = setupRenderWorld(level, contraption);
	}

	public static ContraptionRenderInfo get(Contraption contraption) {
		return ContraptionRenderInfoManager.MANAGERS.get(contraption.entity.level()).getRenderInfo(contraption);
	}

	/**
	 * Reset a contraption's renderer.
	 *
	 * @param contraption The contraption to invalidate.
	 * @return true if there was a renderer associated with the given contraption.
	 */
	public static boolean invalidate(Contraption contraption) {
		return ContraptionRenderInfoManager.MANAGERS.get(contraption.entity.level()).invalidate(contraption);
	}

	public boolean isDead() {
		return !contraption.entity.isAliveOrStale();
	}

	public Contraption getContraption() {
		return contraption;
	}

	public VirtualRenderWorld getRenderWorld() {
		return renderWorld;
	}

	public ContraptionMatrices getMatrices() {
		return matrices;
	}

	public SuperByteBuffer getBuffer(RenderType renderType) {
		return CreateClient.BUFFER_CACHE.get(CONTRAPTION, Pair.of(contraption, renderType), () -> buildStructureBuffer(renderType));
	}

	public void invalidate() {
		for (RenderType renderType : RenderType.chunkBufferLayers()) {
			CreateClient.BUFFER_CACHE.invalidate(CONTRAPTION, Pair.of(contraption, renderType));
		}
	}

	public static VirtualRenderWorld setupRenderWorld(Level level, Contraption c) {
		ContraptionWorld contraptionWorld = c.getContraptionWorld();

		BlockPos origin = c.anchor;
		int minBuildHeight = contraptionWorld.getMinBuildHeight();
		int height = contraptionWorld.getHeight();
		VirtualRenderWorld renderWorld = new VirtualRenderWorld(level, minBuildHeight, height, origin) {
			@Override
			public boolean supportsVisualization() {
				return VisualizationManager.supportsVisualization(level);
			}
		};

		renderWorld.setBlockEntities(c.presentBlockEntities.values());
		for (StructureTemplate.StructureBlockInfo info : c.getBlocks()
			.values())
			renderWorld.setBlock(info.pos(), info.state(), 0);

		renderWorld.runLightEngine();
		return renderWorld;
	}

	private SuperByteBuffer buildStructureBuffer(RenderType layer) {
		BlockRenderDispatcher dispatcher = ModelUtil.VANILLA_RENDERER;
		ModelBlockRenderer renderer = dispatcher.getModelRenderer();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		PoseStack poseStack = objects.poseStack;
		RandomSource random = objects.random;
		RenderedBlocks blocks = contraption.getRenderedBlocks();

		ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
		sbbBuilder.begin();

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
					renderer.tesselateBlock(renderWorld, model, state, pos, poseStack, sbbBuilder, true, random, randomSeed, OverlayTexture.NO_OVERLAY, modelData, layer);
					poseStack.popPose();
				}
			}
		}
		ModelBlockRenderer.clearCache();

		return sbbBuilder.end();
	}

	private static class ThreadLocalObjects {
		public final PoseStack poseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();
		public final ShadedBlockSbbBuilder sbbBuilder = new ShadedBlockSbbBuilder();
	}
}
