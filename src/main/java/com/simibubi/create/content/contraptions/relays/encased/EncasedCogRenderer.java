package com.simibubi.create.content.contraptions.relays.encased;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EncasedCogRenderer extends KineticTileEntityRenderer {

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
	protected void renderSafe(KineticTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		if (Backend.canUseInstancing(te.getLevel()))
			return;

		BlockState blockState = te.getBlockState();
		Block block = blockState.getBlock();
		if (!(block instanceof IRotate))
			return;
		IRotate def = (IRotate) block;

		for (Direction d : Iterate.directionsInAxis(getRotationAxisOf(te))) {
			if (!def.hasShaftTowards(te.getLevel(), te.getBlockPos(), blockState, d))
				continue;
			renderRotatingBuffer(te, CachedBufferer.partialFacing(AllBlockPartials.SHAFT_HALF, te.getBlockState(), d),
				ms, buffer.getBuffer(RenderType.solid()), light);
		}
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return CachedBufferer.partialFacingVertical(
			large ? AllBlockPartials.SHAFTLESS_LARGE_COGWHEEL : AllBlockPartials.SHAFTLESS_COGWHEEL, te.getBlockState(),
			Direction.fromAxisAndDirection(te.getBlockState()
				.getValue(EncasedCogwheelBlock.AXIS), AxisDirection.POSITIVE));
	}

}
