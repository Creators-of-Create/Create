package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.CachedPartialBuffers;
import com.simibubi.create.foundation.render.FlwSuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;

import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.utility.math.AngleHelper;
import net.createmod.ponder.utility.WorldTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class StickerRenderer extends SafeTileEntityRenderer<StickerTileEntity> {

	public StickerRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(StickerTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {

		if (Backend.canUseInstancing(te.getLevel())) return;

		BlockState state = te.getBlockState();
		SuperByteBuffer head = CachedPartialBuffers.partial(AllBlockPartials.STICKER_HEAD, state);
		float offset;

		if (te.getLevel() != Minecraft.getInstance().level && !te.isVirtual())
			offset = state.getValue(StickerBlock.EXTENDED) ? 1 : 0;
		else
			offset = te.piston.getValue(WorldTickHolder.getPartialTicks(te.getLevel()));

		Direction facing = state.getValue(StickerBlock.FACING);
		FlwSuperByteBuffer.cast(head).ifPresent(flwBuffer -> flwBuffer
				.nudge(te.hashCode())
				.centre()
				.rotateY(AngleHelper.horizontalAngle(facing))
				.rotateX(AngleHelper.verticalAngle(facing) + 90)
				.unCentre()
				.translate(0, (offset * offset) * 4 / 16f, 0));


		head.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

}
