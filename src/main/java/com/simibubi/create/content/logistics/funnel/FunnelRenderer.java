package com.simibubi.create.content.logistics.funnel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.FlapStuffs;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class FunnelRenderer extends SmartBlockEntityRenderer<FunnelBlockEntity> {

	public FunnelRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(FunnelBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		if (!be.hasFlap() || VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		BlockState blockState = be.getBlockState();
		VertexConsumer vb = buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.solid());
		PartialModel partialModel = (blockState.getBlock() instanceof FunnelBlock ? AllPartialModels.FUNNEL_FLAP
			: AllPartialModels.BELT_FUNNEL_FLAP);
		SuperByteBuffer flapBuffer = CachedBuffers.partial(partialModel, blockState);

		var funnelFacing = FunnelBlock.getFunnelFacing(blockState);
		float f = be.flap.getValue(partialTicks);

		FlapStuffs.renderFlaps(ms, vb, flapBuffer, FlapStuffs.FUNNEL_PIVOT, funnelFacing, f, -be.getFlapOffset(), light);
	}

}
