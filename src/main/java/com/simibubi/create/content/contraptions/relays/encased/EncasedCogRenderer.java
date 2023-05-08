package com.simibubi.create.content.contraptions.relays.encased;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.contraptions.relays.elementary.BracketedKineticBlockEntityRenderer;
import com.simibubi.create.content.contraptions.relays.elementary.SimpleKineticBlockEntity;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EncasedCogRenderer extends KineticBlockEntityRenderer<SimpleKineticBlockEntity> {

	private boolean large;

	public static EncasedCogRenderer small(BlockEntityRendererProvider.Context context) {
		return new EncasedCogRenderer(context, false);
	}

	public static EncasedCogRenderer large(BlockEntityRendererProvider.Context context) {
		return new EncasedCogRenderer(context, true);
	}

	public EncasedCogRenderer(BlockEntityRendererProvider.Context context, boolean large) {
		super(context);
		this.large = large;
	}

	@Override
	protected void renderSafe(SimpleKineticBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
		if (Backend.canUseInstancing(be.getLevel()))
			return;

		BlockState blockState = be.getBlockState();
		Block block = blockState.getBlock();
		if (!(block instanceof IRotate))
			return;
		IRotate def = (IRotate) block;

		Axis axis = getRotationAxisOf(be);
		BlockPos pos = be.getBlockPos();
		float angle = large ? BracketedKineticBlockEntityRenderer.getAngleForLargeCogShaft(be, axis)
			: getAngleForTe(be, pos, axis);

		for (Direction d : Iterate.directionsInAxis(getRotationAxisOf(be))) {
			if (!def.hasShaftTowards(be.getLevel(), be.getBlockPos(), blockState, d))
				continue;
			SuperByteBuffer shaft = CachedBufferer.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), d);
			kineticRotationTransform(shaft, be, axis, angle, light);
			shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
		}
	}

	@Override
	protected SuperByteBuffer getRotatedModel(SimpleKineticBlockEntity be, BlockState state) {
		return CachedBufferer.partialFacingVertical(
			large ? AllPartialModels.SHAFTLESS_LARGE_COGWHEEL : AllPartialModels.SHAFTLESS_COGWHEEL, state,
			Direction.fromAxisAndDirection(state.getValue(EncasedCogwheelBlock.AXIS), AxisDirection.POSITIVE));
	}

}
