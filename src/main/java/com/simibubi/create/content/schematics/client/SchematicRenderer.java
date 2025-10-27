package com.simibubi.create.content.schematics.client;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.BlockEntityRenderHelper;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.ShadedBlockSbbBuilder;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import net.neoforged.neoforge.client.model.data.ModelData;

public class SchematicRenderer {

	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	private final Map<RenderType, SuperByteBuffer> bufferCache = new LinkedHashMap<>(getLayerCount());
	private boolean changed;
	protected final SchematicLevel schematic;
	private final BlockPos anchor;
	private final List<BlockEntity> renderedBlockEntities = new ArrayList<>();
	private final BitSet shouldRenderBlockEntities = new BitSet();
	private final BitSet scratchErroredBlockEntities = new BitSet();

	public SchematicRenderer(SchematicLevel world) {
		this.anchor = world.anchor;
		this.schematic = world;
		this.changed = true;

		for (var renderedBlockEntity : schematic.getRenderedBlockEntities()) {
			renderedBlockEntities.add(renderedBlockEntity);
		}
		shouldRenderBlockEntities.set(0, renderedBlockEntities.size());
	}

	public void update() {
		changed = true;
	}

	public void render(PoseStack ms, SuperRenderTypeBuffer buffers) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || mc.player == null)
			return;
		if (changed)
			redraw();
		changed = false;

		bufferCache.forEach((layer, buffer) -> {
			buffer.renderInto(ms, buffers.getBuffer(layer));
		});
		scratchErroredBlockEntities.clear();
		BlockEntityRenderHelper.renderBlockEntities(renderedBlockEntities, shouldRenderBlockEntities, scratchErroredBlockEntities, null, schematic, ms, null, buffers, AnimationTickHolder.getPartialTicks());

		// Don't bother looping over errored BEs again.
		shouldRenderBlockEntities.andNot(scratchErroredBlockEntities);
	}

	protected void redraw() {
		bufferCache.clear();

		for (RenderType layer : RenderType.chunkBufferLayers()) {
			SuperByteBuffer buffer = drawLayer(layer);
			if (!buffer.isEmpty())
				bufferCache.put(layer, buffer);
		}
	}

	protected SuperByteBuffer drawLayer(RenderType layer) {
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		ModelBlockRenderer renderer = dispatcher.getModelRenderer();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		PoseStack poseStack = objects.poseStack;
		RandomSource random = objects.random;
		BlockPos.MutableBlockPos mutableBlockPos = objects.mutableBlockPos;
		SchematicLevel renderWorld = schematic;
		BoundingBox bounds = renderWorld.getBounds();

		ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
		sbbBuilder.begin();

		renderWorld.renderMode = true;
		ModelBlockRenderer.enableCaching();
		for (BlockPos localPos : BlockPos.betweenClosed(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ())) {
			BlockPos pos = mutableBlockPos.setWithOffset(localPos, anchor);
			BlockState state = renderWorld.getBlockState(pos);

			if (state.getRenderShape() == RenderShape.MODEL) {
				BakedModel model = dispatcher.getBlockModel(state);
				BlockEntity blockEntity = renderWorld.getBlockEntity(localPos);
				ModelData modelData = blockEntity != null ? blockEntity.getModelData() : ModelData.EMPTY;
				modelData = model.getModelData(renderWorld, pos, state, modelData);
				long seed = state.getSeed(pos);
				random.setSeed(seed);
				if (model.getRenderTypes(state, random, modelData).contains(layer)) {
					poseStack.pushPose();
					poseStack.translate(localPos.getX(), localPos.getY(), localPos.getZ());

					renderer.tesselateBlock(renderWorld, model, state, pos, poseStack, sbbBuilder, true,
						random, seed, OverlayTexture.NO_OVERLAY, modelData, layer);

					poseStack.popPose();
				}
			}
		}
		ModelBlockRenderer.clearCache();
		renderWorld.renderMode = false;

		return sbbBuilder.end();
	}

	private static int getLayerCount() {
		return RenderType.chunkBufferLayers()
			.size();
	}

	private static class ThreadLocalObjects {
		public final PoseStack poseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();
		public final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		public final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
	}

}
