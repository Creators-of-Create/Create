package com.simibubi.create.content.trains.bogey;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class BogeyBlockEntityRenderer<T extends AbstractBogeyBlockEntity> extends SafeBlockEntityRenderer<T> {
	public BogeyBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		BlockState blockState = be.getBlockState();
		if (!(blockState.getBlock() instanceof AbstractBogeyBlock<?> bogey)) {
			return;
		}

		float angle = be.getVirtualAngle(partialTicks);
		ms.pushPose();
		ms.translate(.5f, .5f, .5f);
		if (blockState.getValue(AbstractBogeyBlock.AXIS) == Direction.Axis.X)
			ms.mulPose(Axis.YP.rotationDegrees(90));
		be.getStyle().render(bogey.getSize(), partialTicks, ms, buffer, light, overlay, angle, be.getBogeyData(), false);
		ms.popPose();
	}
}
