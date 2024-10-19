package com.simibubi.create.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BogeyBlockEntityRenderer<T extends BlockEntity> extends SafeBlockEntityRenderer<T> {
	public BogeyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		BlockState blockState = be.getBlockState();
		if (be instanceof AbstractBogeyBlockEntity sbbe) {
			float angle = sbbe.getVirtualAngle(partialTicks);
			if (blockState.getBlock() instanceof AbstractBogeyBlock<?> bogey) {
				ms.translate(.5f, .5f, .5f);
				if (blockState.getValue(AbstractBogeyBlock.AXIS) == Direction.Axis.X)
					ms.mulPose(Axis.YP.rotationDegrees(90));
				sbbe.getStyle().render(bogey.getSize(), partialTicks, ms, buffer, light, overlay, angle, sbbe.getBogeyData(), false);
			}
		}
	}
}
