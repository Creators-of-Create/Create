package com.simibubi.create.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;

import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.data.Iterate;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class StandardBogeyRenderer implements BogeyRenderer {
	@Override
	public void render(CompoundTag bogeyData, float wheelAngle, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay, boolean inContraption) {
		VertexConsumer buffer = bufferSource.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.cutoutMipped());

		SuperByteBuffer shaft = CachedBuffers.block(AllBlocks.SHAFT.getDefaultState()
				.setValue(ShaftBlock.AXIS, Direction.Axis.Z));
		for (int i : Iterate.zeroAndOne) {
			shaft.translate(-.5f, .25f, i * -1)
					.center()
					.rotateZDegrees(wheelAngle)
					.uncenter()
					.light(light)
					.overlay(overlay)
					.renderInto(poseStack, buffer);
		}
	}

	public static class Small extends StandardBogeyRenderer {
		@Override
		public void render(CompoundTag bogeyData, float wheelAngle, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay, boolean inContraption) {
			super.render(bogeyData, wheelAngle, partialTick, poseStack, bufferSource, light, overlay, inContraption);

			VertexConsumer buffer = bufferSource.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.cutoutMipped());

			CachedBuffers.partial(AllPartialModels.BOGEY_FRAME, Blocks.AIR.defaultBlockState())
					.scale(1 - 1 / 512f)
					.light(light)
					.overlay(overlay)
					.renderInto(poseStack, buffer);

			SuperByteBuffer wheels = CachedBuffers.partial(AllPartialModels.SMALL_BOGEY_WHEELS, Blocks.AIR.defaultBlockState());
			for (int side : Iterate.positiveAndNegative) {
				wheels.translate(0, 12 / 16f, side)
					.rotateXDegrees(wheelAngle)
					.light(light)
					.overlay(overlay)
					.renderInto(poseStack, buffer);
			}
		}
	}

	public static class Large extends StandardBogeyRenderer {
		public static final float BELT_RADIUS_PX = 5f;
		public static final float BELT_RADIUS_IN_UV_SPACE = BELT_RADIUS_PX / 16f;

		@Override
		public void render(CompoundTag bogeyData, float wheelAngle, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay, boolean inContraption) {
			super.render(bogeyData, wheelAngle, partialTick, poseStack, bufferSource, light, overlay, inContraption);

			VertexConsumer buffer = bufferSource.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.cutoutMipped());

			SuperByteBuffer secondaryShaft = CachedBuffers.block(AllBlocks.SHAFT.getDefaultState()
					.setValue(ShaftBlock.AXIS, Direction.Axis.X));
			for (int i : Iterate.zeroAndOne) {
				secondaryShaft.translate(-.5f, .25f, .5f + i * -2)
						.center()
						.rotateXDegrees(wheelAngle)
						.uncenter()
						.light(light)
						.overlay(overlay)
						.renderInto(poseStack, buffer);
			}

			CachedBuffers.partial(AllPartialModels.BOGEY_DRIVE, Blocks.AIR.defaultBlockState())
					.scale(1 - 1 / 512f)
					.light(light)
					.overlay(overlay)
					.renderInto(poseStack, buffer);

			float spriteSize = AllSpriteShifts.BOGEY_BELT.getTarget()
				.getV1()
				- AllSpriteShifts.BOGEY_BELT.getTarget()
				.getV0();

			float scroll = BELT_RADIUS_IN_UV_SPACE * Mth.DEG_TO_RAD * wheelAngle;
			scroll = scroll - Mth.floor(scroll);
			scroll = scroll * spriteSize * 0.5f;

			CachedBuffers.partial(AllPartialModels.BOGEY_DRIVE_BELT, Blocks.AIR.defaultBlockState())
					.scale(1 - 1 / 512f)
					.light(light)
					.overlay(overlay)
					.shiftUVScrolling(AllSpriteShifts.BOGEY_BELT, scroll)
					.renderInto(poseStack, buffer);

			CachedBuffers.partial(AllPartialModels.BOGEY_PISTON, Blocks.AIR.defaultBlockState())
					.translate(0, 0, 1 / 4f * Math.sin(AngleHelper.rad(wheelAngle)))
					.light(light)
					.overlay(overlay)
					.renderInto(poseStack, buffer);

			CachedBuffers.partial(AllPartialModels.LARGE_BOGEY_WHEELS, Blocks.AIR.defaultBlockState())
					.translate(0, 1, 0)
					.rotateXDegrees(wheelAngle)
					.light(light)
					.overlay(overlay)
					.renderInto(poseStack, buffer);

			CachedBuffers.partial(AllPartialModels.BOGEY_PIN, Blocks.AIR.defaultBlockState())
					.translate(0, 1, 0)
					.rotateXDegrees(wheelAngle)
					.translate(0, 1 / 4f, 0)
					.rotateXDegrees(-wheelAngle)
					.light(light)
					.overlay(overlay)
					.renderInto(poseStack, buffer);
		}
	}
}
