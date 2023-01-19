package com.simibubi.create.content.contraptions.components.structureMovement.bearing;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BearingRenderer extends KineticBlockEntityRenderer {

	public BearingRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(KineticBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {

		if (Backend.canUseInstancing(be.getLevel())) return;

		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		IBearingBlockEntity bearingBE = (IBearingBlockEntity) be;
		final Direction facing = be.getBlockState()
				.getValue(BlockStateProperties.FACING);
		PartialModel top =
				bearingBE.isWoodenTop() ? AllBlockPartials.BEARING_TOP_WOODEN : AllBlockPartials.BEARING_TOP;
		SuperByteBuffer superBuffer = CachedBufferer.partial(top, be.getBlockState());

		float interpolatedAngle = bearingBE.getInterpolatedAngle(partialTicks - 1);
		kineticRotationTransform(superBuffer, be, facing.getAxis(), (float) (interpolatedAngle / 180 * Math.PI), light);

		if (facing.getAxis()
				.isHorizontal())
			superBuffer.rotateCentered(Direction.UP,
					AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())));
		superBuffer.rotateCentered(Direction.EAST, AngleHelper.rad(-90 - AngleHelper.verticalAngle(facing)));
		superBuffer.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticBlockEntity be, BlockState state) {
		return CachedBufferer.partialFacing(AllBlockPartials.SHAFT_HALF, state, state
				.getValue(BearingBlock.FACING)
				.getOpposite());
	}

}
