package com.simibubi.create.content.kinetics.gearbox;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedPartialBuffers;

import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.utility.Iterate;
import net.createmod.ponder.utility.WorldTickHolder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class GearboxRenderer extends KineticBlockEntityRenderer<GearboxBlockEntity> {

	public GearboxRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(GearboxBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
			int light, int overlay) {
		if (Backend.canUseInstancing(be.getLevel())) return;

		final Axis boxAxis = be.getBlockState().getValue(BlockStateProperties.AXIS);
		final BlockPos pos = be.getBlockPos();
		float time = WorldTickHolder.getRenderTime(be.getLevel());

		for (Direction direction : Iterate.directions) {
			final Axis axis = direction.getAxis();
			if (boxAxis == axis)
				continue;

			SuperByteBuffer shaft = CachedPartialBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), direction);
			float offset = getRotationOffsetForPosition(be, pos, axis);
			float angle = (time * be.getSpeed() * 3f / 10) % 360;

			if (be.getSpeed() != 0 && be.hasSource()) {
				BlockPos source = be.source.subtract(be.getBlockPos());
				Direction sourceFacing = Direction.getNearest(source.getX(), source.getY(), source.getZ());
				if (sourceFacing.getAxis() == direction.getAxis())
					angle *= sourceFacing == direction ? 1 : -1;
				else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
					angle *= -1;
			}

			angle += offset;
			angle = angle / 180f * (float) Math.PI;

			kineticRotationTransform(shaft, be, axis, angle, light);
			shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
		}
	}

}
