package com.simibubi.create.content.kinetics.transmission;

import com.jozufozu.flywheel.api.visualization.VisualizationManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.Block;

public class SplitShaftRenderer extends KineticBlockEntityRenderer<SplitShaftBlockEntity> {

	public SplitShaftRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(SplitShaftBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
			int light, int overlay) {
		if (VisualizationManager.supportsVisualization(be.getLevel())) return;

		Block block = be.getBlockState().getBlock();
		final Axis boxAxis = ((IRotate) block).getRotationAxis(be.getBlockState());
		final BlockPos pos = be.getBlockPos();
		float time = AnimationTickHolder.getRenderTime(be.getLevel());

		for (Direction direction : Iterate.directions) {
			Axis axis = direction.getAxis();
			if (boxAxis != axis)
				continue;

			float offset = getRotationOffsetForPosition(be, pos, axis);
			float angle = (time * be.getSpeed() * 3f / 10) % 360;
			float modifier = be.getRotationSpeedModifier(direction);

			angle *= modifier;
			angle += offset;
			angle = angle / 180f * (float) Math.PI;

			SuperByteBuffer superByteBuffer =
					CachedBufferer.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), direction);
			kineticRotationTransform(superByteBuffer, be, axis, angle, light);
			superByteBuffer.renderInto(ms, buffer.getBuffer(RenderType.solid()));
		}
	}

}
