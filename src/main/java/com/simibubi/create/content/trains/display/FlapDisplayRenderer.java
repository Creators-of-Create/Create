package com.simibubi.create.content.trains.display;

import java.util.List;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import org.joml.Matrix4f;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.createmod.catnip.api.client.render.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;

public class FlapDisplayRenderer extends KineticBlockEntityRenderer<FlapDisplayBlockEntity> {

	public FlapDisplayRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(FlapDisplayBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		float scale = 1 / 32f;

		if (!be.isController)
			return;

		List<FlapDisplayLayout> lines = be.getLines();

		ms.pushPose();
		TransformStack.of(ms)
			.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(be.getBlockState()
				.getValue(FlapDisplayBlock.HORIZONTAL_FACING)))
			.uncenter()
			.translate(0, 0, -3 / 16f);

		ms.translate(0, 1, 1);
		ms.scale(scale, scale, scale);
		ms.scale(1, -1, 1);
		ms.translate(0, 0, 1 / 2f);

		for (int j = 0; j < lines.size(); j++) {
			List<FlapDisplaySection> line = lines.get(j)
				.getSections();
			int color = be.getLineColor(j);
			ms.pushPose();

			float w = 0;
			for (FlapDisplaySection section : line)
				w += section.getSize() + (section.hasGap ? 8 : 1);
			ms.translate(be.xSize * 16 - w / 2 + 1, 4.5f, 0);

			Pose transform = ms.last();
			FlapDisplayRenderOutput renderOutput = new FlapDisplayRenderOutput(buffer, color, transform.pose(), light,
				j, !be.isSpeedRequirementFulfilled(), be.getLevel(), be.isLineGlowing(j));

			for (int i = 0; i < line.size(); i++) {
				FlapDisplaySection section = line.get(i);
				renderOutput.nextSection(section);
				int ticks = AnimationTickHolder.getTicks();
				String text = section.renderCharsIndividually() || !section.spinning[0] ? section.text
					: section.cyclingOptions[((ticks / 3) + i * 13) % section.cyclingOptions.length];
				StringDecomposer.iterateFormatted(text, Style.EMPTY, renderOutput);
				ms.translate(section.size + (section.hasGap ? 8 : 1), 0, 0);
			}

			ms.popPose();
			ms.translate(0, 16, 0);
		}

		ms.popPose();
	}

	static class FlapDisplayRenderOutput implements FormattedCharSink {

		final MultiBufferSource bufferSource;
		final float r, g, b, a;
		final Matrix4f pose;
		final int light;
		final boolean paused;

		FlapDisplaySection section;
		float x;
		private int lineIndex;
		private Level level;

		public FlapDisplayRenderOutput(MultiBufferSource buffer, int color, Matrix4f pose, int light, int lineIndex,
			boolean paused, Level level, boolean glowing) {
			this.bufferSource = buffer;
			this.lineIndex = lineIndex;
			this.level = level;
			this.a = glowing ? .975f : .85f;
			this.r = (color >> 16 & 255) / 255f;
			this.g = (color >> 8 & 255) / 255f;
			this.b = (color & 255) / 255f;
			this.pose = pose;
			this.light = glowing ? 0xf000f0 : light;
			this.paused = paused;
		}

		public void nextSection(FlapDisplaySection section) {
			this.section = section;
			x = 0;
		}

		public boolean accept(int charIndex, Style style, int glyph) {
			int ticks = paused ? 0 : AnimationTickHolder.getTicks();
			float time = paused ? 0 : AnimationTickHolder.getRenderTime();
			float dim = 1;
			float standardWidth = section.wideFlaps ? FlapDisplaySection.WIDE_MONOSPACE : FlapDisplaySection.MONOSPACE;
			float glyphWidth = standardWidth;

			if (section.renderCharsIndividually() && section.spinning[Math.min(charIndex, section.spinning.length)]) {
				float speed = section.spinningTicks > 5 && section.spinningTicks < 20 ? 1.75f : 2.5f;
				float cycle = (time / speed) + charIndex * 16.83f + lineIndex * 0.75f;
				float partial = cycle % 1;
				char cyclingGlyph = section.cyclingOptions[((int) cycle) % section.cyclingOptions.length].charAt(0);
				glyph = paused ? cyclingGlyph : partial > 1 / 2f ? partial > 3 / 4f ? '_' : '-' : cyclingGlyph;
				dim = 0.75f;
			}

			if (!section.renderCharsIndividually() && section.spinning[0]) {
				glyph = ticks % 3 == 0 ? glyphWidth == 6 ? '-' : glyphWidth == 1 ? '\'' : glyph : glyph;
				glyph = ticks % 3 == 2 ? glyphWidth == 6 ? '_' : glyphWidth == 1 ? '.' : glyph : glyph;
				if (ticks % 3 != 1)
					dim = 0.75f;
			}

			TextColor textcolor = style.getColor();

			float red = this.r * dim;
			float green = this.g * dim;
			float blue = this.b * dim;

			if (textcolor != null) {
				int i = textcolor.getValue();
				red = (i >> 16 & 255) / 255f;
				green = (i >> 8 & 255) / 255f;
				blue = (i & 255) / 255f;
			}

			if (section.renderCharsIndividually())
				x += (standardWidth - glyphWidth) / 2f;

			// TODO 26.2: Rebuild flap text submission on the new Font render-state API.

			if (section.renderCharsIndividually())
				x += standardWidth - (standardWidth - glyphWidth) / 2f;
			else
				x += glyphWidth;

			return true;
		}

		public float finish(int bgColor) {
			if (bgColor == 0)
				return x;

			float a = (bgColor >> 24 & 255) / 255f;
			float r = (bgColor >> 16 & 255) / 255f;
			float g = (bgColor >> 8 & 255) / 255f;
			float b = (bgColor & 255) / 255f;

			return x;
		}

		private FontSet getFontSet() {
			return null;
		}

		private RenderType renderTypeOf(BakedGlyph bakedglyph) {
			return null;
		}

		private boolean isNotEmpty(BakedGlyph bakedglyph) {
			return false;
		}

	}

	@Override
	protected SuperByteBuffer getRotatedModel(FlapDisplayBlockEntity be, BlockState state) {
		return CachedBuffers.partialFacingVertical(AllPartialModels.SHAFTLESS_COGWHEEL, state,
			state.getValue(FlapDisplayBlock.HORIZONTAL_FACING));
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

}
