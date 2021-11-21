package com.simibubi.create.content.contraptions.components.structureMovement.chassis;

import com.jozufozu.flywheel.backend.Backend;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

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

		if (Backend.getInstance().canUseInstancing(te.getLevel())) return;

		BlockState state = te.getBlockState();
		SuperByteBuffer head = CachedBufferer.partial(AllBlockPartials.STICKER_HEAD, state);
		float offset = te.piston.getValue(AnimationTickHolder.getPartialTicks(te.getLevel()));

		if (te.getLevel() != Minecraft.getInstance().level && !te.isVirtual())
			offset = state.getValue(StickerBlock.EXTENDED) ? 1 : 0;

		Direction facing = state.getValue(StickerBlock.FACING);
		head.matrixStacker()
			.nudge(te.hashCode())
			.centre()
			.rotateY(AngleHelper.horizontalAngle(facing))
			.rotateX(AngleHelper.verticalAngle(facing) + 90)
			.unCentre()
			.translate(0, (offset * offset) * 4 / 16f, 0);

		head.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

}
