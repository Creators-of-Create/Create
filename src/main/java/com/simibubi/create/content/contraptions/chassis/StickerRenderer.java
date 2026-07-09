package com.simibubi.create.content.contraptions.chassis;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class StickerRenderer extends SafeBlockEntityRenderer<StickerBlockEntity> {

	public StickerRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(StickerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {

		if (VisualizationManager.supportsVisualization(be.getLevel())) return;

		BlockState state = be.getBlockState();
		SuperByteBuffer head = CachedBuffers.partial(AllPartialModels.STICKER_HEAD, state);
		float offset = be.piston.getValue(AnimationTickHolder.getPartialTicks());

		if (be.getLevel() != Minecraft.getInstance().level && !be.isVirtual())
			offset = state.getValue(StickerBlock.EXTENDED) ? 1 : 0;

		Direction facing = state.getValue(StickerBlock.FACING);
		head.nudge(be.hashCode())
			.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(facing))
			.rotateXDegrees(AngleHelper.verticalAngle(facing) + 90)
			.uncenter()
			.translate(0, (offset * offset) * 4 / 16f, 0);

		head.light(light)
			.renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.solid()));
	}

}
