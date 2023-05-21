package com.simibubi.create.content.contraptions.render;

import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.jozufozu.flywheel.event.RenderLayerEvent;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.SuperByteBufferCache;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.LevelAccessor;

public class SBBContraptionManager extends ContraptionRenderingWorld<ContraptionRenderInfo> {
	public static final SuperByteBufferCache.Compartment<Pair<Contraption, RenderType>> CONTRAPTION = new SuperByteBufferCache.Compartment<>();

	public SBBContraptionManager(LevelAccessor world) {
		super(world);
	}

	@Override
	public void renderLayer(RenderLayerEvent event) {
		super.renderLayer(event);
		RenderType type = event.getType();
		VertexConsumer consumer = event.buffers.bufferSource()
				.getBuffer(type);
		visible.forEach(info -> renderContraptionLayerSBB(info, type, consumer));

		event.buffers.bufferSource().endBatch(type);
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

		SuperByteBuffer contraptionBuffer = CreateClient.BUFFER_CACHE.get(CONTRAPTION, Pair.of(renderInfo.contraption, layer), () -> ContraptionRenderDispatcher.buildStructureBuffer(renderInfo.renderWorld, renderInfo.contraption, layer));

		if (!contraptionBuffer.isEmpty()) {
			ContraptionMatrices matrices = renderInfo.getMatrices();

			contraptionBuffer.transform(matrices.getModel())
					.light(matrices.getWorld())
					.hybridLight()
					.renderInto(matrices.getViewProjection(), consumer);
		}

	}
}
