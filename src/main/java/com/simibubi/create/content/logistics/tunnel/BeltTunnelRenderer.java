package com.simibubi.create.content.logistics.tunnel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.FlapStuffs;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.data.Iterate;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class BeltTunnelRenderer extends SmartBlockEntityRenderer<BeltTunnelBlockEntity> {

	public BeltTunnelRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(BeltTunnelBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;

		SuperByteBuffer flapBuffer = CachedBuffers.partial(AllPartialModels.BELT_TUNNEL_FLAP, be.getBlockState());
		VertexConsumer vb = buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.solid());

		for (Direction direction : Iterate.directions) {
			if (!be.flaps.containsKey(direction))
				continue;

			float f = be.flaps.get(direction)
				.getValue(partialTicks);

			FlapStuffs.renderFlaps(ms, vb, flapBuffer, FlapStuffs.TUNNEL_PIVOT, direction, f, 0, light);
		}

	}

}
