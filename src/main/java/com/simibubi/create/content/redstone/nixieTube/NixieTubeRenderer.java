package com.simibubi.create.content.redstone.nixieTube;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.redstone.nixieTube.DoubleFaceAttachedBlock.DoubleAttachFace;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.RenderTypes;
import com.simibubi.create.foundation.utility.DyeHelper;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Style;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class NixieTubeRenderer extends SafeBlockEntityRenderer<NixieTubeBlockEntity> {
	private static final int GLOW_VIEW_DISTANCE = 96;

	public NixieTubeRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(NixieTubeBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		ms.pushPose();
		BlockState blockState = be.getBlockState();
		DoubleAttachFace face = blockState.getValue(NixieTubeBlock.FACE);
		float yRot = AngleHelper.horizontalAngle(blockState.getValue(NixieTubeBlock.FACING)) - 90
			+ (face == DoubleAttachFace.WALL_REVERSED ? 180 : 0);
		float xRot = face == DoubleAttachFace.WALL ? -90 : face == DoubleAttachFace.WALL_REVERSED ? 90 : 0;

		var msr = TransformStack.of(ms);
		msr.center()
			.rotateYDegrees(yRot)
			.rotateZDegrees(xRot)
			.uncenter();

		if (be.signalState != null || be.computerSignal != null) {
			renderAsSignal(be, partialTicks, ms, buffer, light, overlay);
			ms.popPose();
			return;
		}

		msr.center();

		float height = face == DoubleAttachFace.CEILING ? 5 : 3;
		float scale = 1 / 20f;

		Couple<String> s = be.getDisplayedStrings();
		DyeColor color = NixieTubeBlock.colorOf(be.getBlockState());
		RandomSource random = be.getLevel().getRandom();

		ms.pushPose();
		ms.translate(-4 / 16f, 0, 0);
		ms.scale(scale, -scale, scale);
		drawTube(ms, buffer, s.getFirst(), height, color, random);
		ms.popPose();

		ms.pushPose();
		ms.translate(4 / 16f, 0, 0);
		ms.scale(scale, -scale, scale);
		drawTube(ms, buffer, s.getSecond(), height, color, random);
		ms.popPose();

		ms.popPose();
	}

	public static void drawTube(PoseStack ms, MultiBufferSource buffer, String c, float height, DyeColor color, RandomSource random) {
		Font fontRenderer = Minecraft.getInstance().font;
		float charWidth = fontRenderer.width(c);
		float shadowOffset = .5f;
		float flicker = random.nextFloat();
		Couple<Integer> couple = DyeHelper.getDyeColors(color);
		int brightColor = couple.getFirst();
		int darkColor = couple.getSecond();
		int flickeringBrightColor = Color.mixColors(brightColor, darkColor, flicker / 4);

		ms.pushPose();
		ms.translate((charWidth - shadowOffset) / -2f, -height, 0);
		drawInWorldString(ms, buffer, c, flickeringBrightColor);
		ms.pushPose();
		ms.translate(shadowOffset, shadowOffset, -1 / 16f);
		drawInWorldString(ms, buffer, c, darkColor);
		ms.popPose();
		ms.popPose();

		ms.pushPose();
		ms.scale(-1, 1, 1);
		ms.translate((charWidth - shadowOffset) / -2f, -height, 0);
		drawInWorldString(ms, buffer, c, darkColor);
		ms.pushPose();
		ms.translate(-shadowOffset, shadowOffset, -1 / 16f);
		drawInWorldString(ms, buffer, c, Color.mixColors(darkColor, 0, .35f));
		ms.popPose();
		ms.popPose();
	}

	public static void drawInWorldString(PoseStack ms, MultiBufferSource buffer, String c, int color) {
		Font fontRenderer = Minecraft.getInstance().font;
		fontRenderer.drawInBatch(c, 0, 0, color, false, ms.last()
			.pose(), buffer, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
		if (buffer instanceof BufferSource) {
			BakedGlyph texturedglyph = fontRenderer.getFontSet(Style.DEFAULT_FONT)
				.whiteGlyph();
			((BufferSource) buffer).endBatch(texturedglyph.renderType(Font.DisplayMode.NORMAL));
		}
	}

	private void renderAsSignal(NixieTubeBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		BlockState blockState = be.getBlockState();
		Direction facing = NixieTubeBlock.getFacing(blockState);
		Vec3 observerVec = Minecraft.getInstance().cameraEntity.getEyePosition(partialTicks);
		var msr = TransformStack.of(ms);

		if (facing == Direction.DOWN)
			msr.center()
				.rotateZDegrees(180)
				.uncenter();

		boolean invertTubes =
			facing == Direction.DOWN || blockState.getValue(NixieTubeBlock.FACE) == DoubleAttachFace.WALL_REVERSED;

		CachedBuffers.partial(AllPartialModels.SIGNAL_PANEL, blockState)
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));

		ms.pushPose();
		ms.translate(1 / 2f, 7.5f / 16f, 1 / 2f);
		float renderTime = AnimationTickHolder.getRenderTime(be.getLevel());
		Vec3 lampVec = Vec3.atCenterOf(be.getBlockPos());
		Vec3 diff = lampVec.subtract(observerVec);

		if (be.signalState != null) {
			for (boolean first : Iterate.trueAndFalse) {
				if (first && !be.signalState.isRedLight(renderTime))
					continue;
				if (!first && !be.signalState.isGreenLight(renderTime) && !be.signalState.isYellowLight(renderTime))
					continue;

				boolean flip = first == invertTubes;
				boolean yellow = be.signalState.isYellowLight(renderTime);

				ms.pushPose();
				ms.translate(flip ? 4 / 16f : -4 / 16f, 0, 0);

				if (diff.lengthSqr() < GLOW_VIEW_DISTANCE * GLOW_VIEW_DISTANCE) {
					boolean vert = first ^ facing.getAxis()
						.isHorizontal();
					float longSide = yellow ? 1 : 4;
					float longSideGlow = yellow ? 2 : 5.125f;

					CachedBuffers.partial(AllPartialModels.SIGNAL_WHITE_CUBE, blockState)
						.light(0xf000f0)
						.disableDiffuse()
						.scale(vert ? longSide : 1, vert ? 1 : longSide, 1)
						.renderInto(ms, buffer.getBuffer(RenderType.translucent()));

					CachedBuffers
						.partial(
							first ? AllPartialModels.SIGNAL_RED_GLOW
								: yellow ? AllPartialModels.SIGNAL_YELLOW_GLOW : AllPartialModels.SIGNAL_WHITE_GLOW,
							blockState)
						.light(0xf000f0)
						.disableDiffuse()
						.scale(vert ? longSideGlow : 2, vert ? 2 : longSideGlow, 2)
						.renderInto(ms, buffer.getBuffer(RenderTypes.additive()));
				}

				CachedBuffers
					.partial(first ? AllPartialModels.SIGNAL_RED
						: yellow ? AllPartialModels.SIGNAL_YELLOW : AllPartialModels.SIGNAL_WHITE, blockState)
					.light(0xF000F0)
					.disableDiffuse()
					.scale(1 + 1 / 16f)
					.renderInto(ms, buffer.getBuffer(RenderTypes.additive()));

				ms.popPose();
			}
		} else if (be.computerSignal != null) {
			for (boolean first : Iterate.trueAndFalse) {
				NixieTubeBlockEntity.ComputerSignal.TubeDisplay tubeDisplay = first ?
					be.computerSignal.first : be.computerSignal.second;
				if (tubeDisplay.blinkPeriod == 0 || tubeDisplay.blinkPeriod > 1 && renderTime % tubeDisplay.blinkPeriod < tubeDisplay.blinkOffTime)
					continue;

				boolean flip = first == invertTubes;

				ms.pushPose();
				ms.translate(flip ? 4 / 16f : -4 / 16f, 0, 0);

				if (diff.lengthSqr() < GLOW_VIEW_DISTANCE * GLOW_VIEW_DISTANCE) {
					boolean horiz = facing.getAxis().isHorizontal();
					float width = horiz ? tubeDisplay.glowWidth : tubeDisplay.glowHeight;
					float height = horiz ? tubeDisplay.glowHeight : tubeDisplay.glowWidth;

					CachedBuffers.partial(AllPartialModels.SIGNAL_COMPUTER_WHITE_CUBE, blockState)
						.light(0xf000f0)
						.disableDiffuse()
						.scale(width, height,  1)
						.renderInto(ms, buffer.getBuffer(RenderType.translucent()));

					CachedBuffers
						.partial(AllPartialModels.SIGNAL_COMPUTER_WHITE_GLOW, blockState)
						.light(0xf000f0)
						.color(
							Math.min(((tubeDisplay.r & 0xFF) * 6 + 256) >> 3, 255),
							Math.min(((tubeDisplay.g & 0xFF) * 6 + 256) >> 3, 255),
							Math.min(((tubeDisplay.b & 0xFF) * 6 + 256) >> 3, 255),
							255)
						.disableDiffuse()
						.scale(width + 1.125f, height + 1.125f, 2)
						.renderInto(ms, buffer.getBuffer(RenderTypes.additive()));
				}

				CachedBuffers
					.partial(AllPartialModels.SIGNAL_COMPUTER_WHITE_BASE, blockState)
					.light(0xF000F0)
					.color(12, 12, 12, 255)
					.disableDiffuse()
					.scale(1 + 1.25f / 16f)
					.renderInto(ms, buffer.getBuffer(RenderTypes.additive()));

				CachedBuffers
					.partial(AllPartialModels.SIGNAL_COMPUTER_WHITE, blockState)
					.light(0xF000F0)
					.color(tubeDisplay.r, tubeDisplay.g, tubeDisplay.b, 255)
					.disableDiffuse()
					.scale(1 + 1 / 16f)
					.renderInto(ms, buffer.getBuffer(RenderTypes.additive()));

				ms.popPose();
			}
		}

		ms.popPose();

	}

	@Override
	public int getViewDistance() {
		return 128;
	}

}
