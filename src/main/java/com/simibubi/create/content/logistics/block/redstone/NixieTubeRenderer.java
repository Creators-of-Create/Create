package com.simibubi.create.content.logistics.block.redstone;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.ImmutableMap;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.logistics.block.redstone.DoubleFaceAttachedBlock.DoubleAttachFace;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.RenderTypes;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;

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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class NixieTubeRenderer extends SafeTileEntityRenderer<NixieTubeTileEntity> {

	private Random r = new Random();

	public static final Map<DyeColor, Couple<Integer>> DYE_TABLE = new ImmutableMap.Builder<DyeColor, Couple<Integer>>()

		// DyeColor, ( Front RGB, Back RGB )
		.put(DyeColor.BLACK, Couple.create(0x45403B, 0x21201F))
		.put(DyeColor.RED, Couple.create(0xB13937, 0x632737))
		.put(DyeColor.GREEN, Couple.create(0x208A46, 0x1D6045))
		.put(DyeColor.BROWN, Couple.create(0xAC855C, 0x68533E))

		.put(DyeColor.BLUE, Couple.create(0x5391E1, 0x504B90))
		.put(DyeColor.GRAY, Couple.create(0x5D666F, 0x313538))
		.put(DyeColor.LIGHT_GRAY, Couple.create(0x95969B, 0x707070))
		.put(DyeColor.PURPLE, Couple.create(0x9F54AE, 0x63366C))

		.put(DyeColor.CYAN, Couple.create(0x3EABB4, 0x3C7872))
		.put(DyeColor.PINK, Couple.create(0xD5A8CB, 0xB86B95))
		.put(DyeColor.LIME, Couple.create(0xA3DF55, 0x4FB16F))
		.put(DyeColor.YELLOW, Couple.create(0xE6D756, 0xE9AC29))

		.put(DyeColor.LIGHT_BLUE, Couple.create(0x69CED2, 0x508AA5))
		.put(DyeColor.ORANGE, Couple.create(0xEE9246, 0xD94927))
		.put(DyeColor.MAGENTA, Couple.create(0xF062B0, 0xC04488))
		.put(DyeColor.WHITE, Couple.create(0xEDEAE5, 0xBBB6B0))

		.build();

	public NixieTubeRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(NixieTubeTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		ms.pushPose();
		BlockState blockState = te.getBlockState();
		DoubleAttachFace face = blockState.getValue(NixieTubeBlock.FACE);
		float yRot = AngleHelper.horizontalAngle(blockState.getValue(NixieTubeBlock.FACING)) - 90
			+ (face == DoubleAttachFace.WALL_REVERSED ? 180 : 0);
		float xRot = face == DoubleAttachFace.WALL ? -90 : face == DoubleAttachFace.WALL_REVERSED ? 90 : 0;

		TransformStack msr = TransformStack.cast(ms);
		msr.centre()
			.rotateY(yRot)
			.rotateZ(xRot)
			.unCentre();

		if (te.signalState != null) {
			renderAsSignal(te, partialTicks, ms, buffer, light, overlay);
			ms.popPose();
			return;
		}

		msr.centre();

		float height = face == DoubleAttachFace.CEILING ? 5 : 3;
		float scale = 1 / 20f;

		Couple<String> s = te.getDisplayedStrings();
		DyeColor color = NixieTubeBlock.colorOf(te.getBlockState());

		ms.pushPose();
		ms.translate(-4 / 16f, 0, 0);
		ms.scale(scale, -scale, scale);
		drawTube(ms, buffer, s.getFirst(), height, color);
		ms.popPose();

		ms.pushPose();
		ms.translate(4 / 16f, 0, 0);
		ms.scale(scale, -scale, scale);
		drawTube(ms, buffer, s.getSecond(), height, color);
		ms.popPose();

		ms.popPose();
	}

	private void drawTube(PoseStack ms, MultiBufferSource buffer, String c, float height, DyeColor color) {
		Font fontRenderer = Minecraft.getInstance().font;
		float charWidth = fontRenderer.width(c);
		float shadowOffset = .5f;
		float flicker = r.nextFloat();
		Couple<Integer> couple = DYE_TABLE.get(color);
		int brightColor = couple.getFirst();
		int darkColor = couple.getSecond();
		int flickeringBrightColor = Color.mixColors(brightColor, darkColor, flicker / 4);

		ms.pushPose();
		ms.translate((charWidth - shadowOffset) / -2f, -height, 0);
		drawChar(ms, buffer, c, flickeringBrightColor);
		ms.pushPose();
		ms.translate(shadowOffset, shadowOffset, -1 / 16f);
		drawChar(ms, buffer, c, darkColor);
		ms.popPose();
		ms.popPose();

		ms.pushPose();
		ms.scale(-1, 1, 1);
		ms.translate((charWidth - shadowOffset) / -2f, -height, 0);
		drawChar(ms, buffer, c, darkColor);
		ms.pushPose();
		ms.translate(-shadowOffset, shadowOffset, -1 / 16f);
		drawChar(ms, buffer, c, Color.mixColors(darkColor, 0, .35f));
		ms.popPose();
		ms.popPose();
	}

	private static void drawChar(PoseStack ms, MultiBufferSource buffer, String c, int color) {
		Font fontRenderer = Minecraft.getInstance().font;
		fontRenderer.drawInBatch(c, 0, 0, color, false, ms.last()
			.pose(), buffer, false, 0, LightTexture.FULL_BRIGHT);
		if (buffer instanceof BufferSource) {
			BakedGlyph texturedglyph = fontRenderer.getFontSet(Style.DEFAULT_FONT)
				.whiteGlyph();
			((BufferSource) buffer).endBatch(texturedglyph.renderType(Font.DisplayMode.NORMAL));
		}
	}

	private void renderAsSignal(NixieTubeTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		BlockState blockState = te.getBlockState();
		Direction facing = NixieTubeBlock.getFacing(blockState);
		Vec3 observerVec = Minecraft.getInstance().cameraEntity.getEyePosition(partialTicks);
		TransformStack msr = TransformStack.cast(ms);

		if (facing == Direction.DOWN)
			msr.centre()
				.rotateZ(180)
				.unCentre();

		boolean invertTubes =
			facing == Direction.DOWN || blockState.getValue(NixieTubeBlock.FACE) == DoubleAttachFace.WALL_REVERSED;

		CachedBufferer.partial(AllBlockPartials.SIGNAL_PANEL, blockState)
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));

		ms.pushPose();
		ms.translate(1 / 2f, 7.5f / 16f, 1 / 2f);
		float renderTime = AnimationTickHolder.getRenderTime(te.getLevel());

		for (boolean first : Iterate.trueAndFalse) {
			Vec3 lampVec = Vec3.atCenterOf(te.getBlockPos());
			Vec3 diff = lampVec.subtract(observerVec);

			if (first && !te.signalState.isRedLight(renderTime))
				continue;
			if (!first && !te.signalState.isGreenLight(renderTime) && !te.signalState.isYellowLight(renderTime))
				continue;

			boolean flip = first == invertTubes;
			boolean yellow = te.signalState.isYellowLight(renderTime);

			ms.pushPose();
			ms.translate(flip ? 4 / 16f : -4 / 16f, 0, 0);

			if (diff.lengthSqr() < 36 * 36) {
				boolean vert = first ^ facing.getAxis()
					.isHorizontal();
				float longSide = yellow ? 1 : 4;
				float longSideGlow = yellow ? 2 : 5.125f;

				CachedBufferer.partial(AllBlockPartials.SIGNAL_WHITE_CUBE, blockState)
					.light(0xf000f0)
					.disableDiffuseMult()
					.scale(vert ? longSide : 1, vert ? 1 : longSide, 1)
					.renderInto(ms, buffer.getBuffer(RenderType.translucent()));

				CachedBufferer
					.partial(
						first ? AllBlockPartials.SIGNAL_RED_GLOW
							: yellow ? AllBlockPartials.SIGNAL_YELLOW_GLOW : AllBlockPartials.SIGNAL_WHITE_GLOW,
						blockState)
					.light(0xf000f0)
					.disableDiffuseMult()
					.scale(vert ? longSideGlow : 2, vert ? 2 : longSideGlow, 2)
					.renderInto(ms, buffer.getBuffer(RenderTypes.getAdditive()));
			}

			CachedBufferer
				.partial(first ? AllBlockPartials.SIGNAL_RED
					: yellow ? AllBlockPartials.SIGNAL_YELLOW : AllBlockPartials.SIGNAL_WHITE, blockState)
				.light(0xF000F0)
				.disableDiffuseMult()
				.scale(1 + 1 / 16f)
				.renderInto(ms, buffer.getBuffer(RenderTypes.getAdditive()));

			ms.popPose();
		}
		ms.popPose();

	}

}
