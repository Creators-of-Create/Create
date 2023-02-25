package com.simibubi.create.content.contraptions.components.crank;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class HandCrankRenderer extends KineticBlockEntityRenderer<HandCrankBlockEntity> {

	public HandCrankRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(HandCrankBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		if (Backend.canUseInstancing(be.getLevel())) return;

		BlockState state = be.getBlockState();
		Block block = state.getBlock();
		PartialModel renderedHandle = null;
		if (block instanceof HandCrankBlock)
			renderedHandle = ((HandCrankBlock) block).getRenderedHandle();
		if (renderedHandle == null)
			return;

		Direction facing = state.getValue(FACING);
		SuperByteBuffer handle = CachedBufferer.partialFacing(renderedHandle, state, facing.getOpposite());
		kineticRotationTransform(handle, be, facing.getAxis(),
				(be.independentAngle + partialTicks * be.chasingVelocity) / 360, light);
		handle.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

}
