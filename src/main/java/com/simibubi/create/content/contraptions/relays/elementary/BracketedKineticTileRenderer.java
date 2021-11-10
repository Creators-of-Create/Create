package com.simibubi.create.content.contraptions.relays.elementary;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;

public class BracketedKineticTileRenderer extends KineticTileEntityRenderer {

	public BracketedKineticTileRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {

		if (Backend.getInstance()
			.canUseInstancing(te.getLevel()))
			return;

		if (!AllBlocks.LARGE_COGWHEEL.has(te.getBlockState())) {
			super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
			return;
		}

		// Large cogs sometimes have to offset their teeth by 11.25 degrees in order to
		// mesh properly

		Axis axis = getRotationAxisOf(te);
		BlockPos pos = te.getBlockPos();

		Direction facing = Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE);
		renderRotatingBuffer(te,
			PartialBufferer.getFacingVertical(AllBlockPartials.SHAFTLESS_LARGE_COGWHEEL, te.getBlockState(), facing),
			ms, buffer.getBuffer(RenderType.solid()), light);

		float offset = getShaftAngleOffset(axis, pos);
		float time = AnimationTickHolder.getRenderTime(te.getLevel());
		float angle = ((time * te.getSpeed() * 3f / 10 + offset) % 360) / 180 * (float) Math.PI;

		SuperByteBuffer shaft =
			PartialBufferer.getFacingVertical(AllBlockPartials.COGWHEEL_SHAFT, te.getBlockState(), facing);
		kineticRotationTransform(shaft, te, axis, angle, light);
		shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));

	}

	public static float getShaftAngleOffset(Axis axis, BlockPos pos) {
		float offset = 0;
		double d = (((axis == Axis.X) ? 0 : pos.getX()) + ((axis == Axis.Y) ? 0 : pos.getY())
			+ ((axis == Axis.Z) ? 0 : pos.getZ())) % 2;
		if (d == 0)
			offset = 22.5f;
		return offset;
	}

}
