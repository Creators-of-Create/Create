package com.simibubi.create.content.contraptions.chassis;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedPartialBuffers;

import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.utility.math.AngleHelper;
import net.createmod.ponder.utility.WorldTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class StickerRenderer extends SafeBlockEntityRenderer<StickerBlockEntity> {

	public StickerRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(StickerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {

		if (Backend.canUseInstancing(be.getLevel())) return;

		BlockState state = be.getBlockState();
		SuperByteBuffer head = CachedPartialBuffers.partial(AllPartialModels.STICKER_HEAD, state);
		float offset = be.piston.getValue(WorldTickHolder.getPartialTicks(be.getLevel()));

		if (be.getLevel() != Minecraft.getInstance().level && !be.isVirtual())
			offset = state.getValue(StickerBlock.EXTENDED) ? 1 : 0;

		Direction facing = state.getValue(StickerBlock.FACING);
		head.nudge(be.hashCode())
			.centre()
			.rotateY(AngleHelper.horizontalAngle(facing))
			.rotateX(AngleHelper.verticalAngle(facing) + 90)
			.unCentre()
			.translate(0, (offset * offset) * 4 / 16f, 0);

		head.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

}
