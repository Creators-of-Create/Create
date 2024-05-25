package com.simibubi.create.content.processing.burner;

import javax.annotation.Nullable;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlazeBurnerRenderer extends SafeBlockEntityRenderer<BlazeBurnerBlockEntity> {

	public BlazeBurnerRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(BlazeBurnerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource,
		int light, int overlay) {
		HeatLevel heatLevel = be.getHeatLevelFromBlock();
		if (heatLevel == HeatLevel.NONE)
			return;

		Level level = be.getLevel();
		BlockState blockState = be.getBlockState();
		float animation = be.headAnimation.getValue(partialTicks) * .175f;
		float horizontalAngle = AngleHelper.rad(be.headAngle.getValue(partialTicks));
		boolean canDrawFlame = heatLevel.isAtLeast(HeatLevel.FADING);
		boolean drawGoggles = be.goggles;
		boolean drawHat = be.hat;
		int hashCode = be.hashCode();

		renderShared(ms, null, bufferSource,
			level, blockState, heatLevel, animation, horizontalAngle,
			canDrawFlame, drawGoggles, drawHat, hashCode);
	}

	public static void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource bufferSource, LerpedFloat headAngle, boolean conductor) {
		BlockState state = context.state;
		HeatLevel heatLevel = BlazeBurnerBlock.getHeatLevelOf(state);
		if (heatLevel == HeatLevel.NONE)
			return;

		if (!heatLevel.isAtLeast(HeatLevel.FADING)) {
			heatLevel = HeatLevel.FADING;
		}

		Level level = context.world;
		float horizontalAngle = AngleHelper.rad(headAngle.getValue(AnimationTickHolder.getPartialTicks(level)));
		boolean drawGoggles = context.blockEntityData.contains("Goggles");
		boolean drawHat = conductor || context.blockEntityData.contains("TrainHat");
		int hashCode = context.hashCode();

		renderShared(matrices.getViewProjection(), matrices.getModel(), bufferSource,
			level, state, heatLevel, 0, horizontalAngle,
			false, drawGoggles, drawHat, hashCode);
	}

	private static void renderShared(PoseStack ms, @Nullable PoseStack modelTransform, MultiBufferSource bufferSource,
		Level level, BlockState blockState, HeatLevel heatLevel, float animation, float horizontalAngle,
		boolean canDrawFlame, boolean drawGoggles, boolean drawHat, int hashCode) {

		boolean blockAbove = animation > 0.125f;
		float time = AnimationTickHolder.getRenderTime(level);
		float renderTick = time + (hashCode % 13) * 16f;
		float offsetMult = heatLevel.isAtLeast(HeatLevel.FADING) ? 64 : 16;
		float offset = Mth.sin((float) ((renderTick / 16f) % (2 * Math.PI))) / offsetMult;
		float offset1 = Mth.sin((float) ((renderTick / 16f + Math.PI) % (2 * Math.PI))) / offsetMult;
		float offset2 = Mth.sin((float) ((renderTick / 16f + Math.PI / 2) % (2 * Math.PI))) / offsetMult;
		float headY = offset - (animation * .75f);

		VertexConsumer solid = bufferSource.getBuffer(RenderType.solid());
		VertexConsumer cutout = bufferSource.getBuffer(RenderType.cutoutMipped());

		ms.pushPose();

		if (canDrawFlame && blockAbove) {
			SpriteShiftEntry spriteShift =
				heatLevel == HeatLevel.SEETHING ? AllSpriteShifts.SUPER_BURNER_FLAME : AllSpriteShifts.BURNER_FLAME;

			float spriteWidth = spriteShift.getTarget()
				.getU1()
				- spriteShift.getTarget()
					.getU0();

			float spriteHeight = spriteShift.getTarget()
				.getV1()
				- spriteShift.getTarget()
					.getV0();

			float speed = 1 / 32f + 1 / 64f * heatLevel.ordinal();

			double vScroll = speed * time;
			vScroll = vScroll - Math.floor(vScroll);
			vScroll = vScroll * spriteHeight / 2;

			double uScroll = speed * time / 2;
			uScroll = uScroll - Math.floor(uScroll);
			uScroll = uScroll * spriteWidth / 2;

			SuperByteBuffer flameBuffer = CachedBufferer.partial(AllPartialModels.BLAZE_BURNER_FLAME, blockState);
			if (modelTransform != null)
				flameBuffer.transform(modelTransform);
			flameBuffer.shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll);
			draw(flameBuffer, horizontalAngle, ms, cutout);
		}

		PartialModel blazeModel;
		if (heatLevel.isAtLeast(HeatLevel.SEETHING)) {
			blazeModel = blockAbove ? AllPartialModels.BLAZE_SUPER_ACTIVE : AllPartialModels.BLAZE_SUPER;
		} else if (heatLevel.isAtLeast(HeatLevel.FADING)) {
			blazeModel = blockAbove && heatLevel.isAtLeast(HeatLevel.KINDLED) ? AllPartialModels.BLAZE_ACTIVE
				: AllPartialModels.BLAZE_IDLE;
		} else {
			blazeModel = AllPartialModels.BLAZE_INERT;
		}

		SuperByteBuffer blazeBuffer = CachedBufferer.partial(blazeModel, blockState);
		if (modelTransform != null)
			blazeBuffer.transform(modelTransform);
		blazeBuffer.translate(0, headY, 0);
		draw(blazeBuffer, horizontalAngle, ms, solid);

		if (drawGoggles) {
			PartialModel gogglesModel = blazeModel == AllPartialModels.BLAZE_INERT
					? AllPartialModels.BLAZE_GOGGLES_SMALL : AllPartialModels.BLAZE_GOGGLES;

			SuperByteBuffer gogglesBuffer = CachedBufferer.partial(gogglesModel, blockState);
			if (modelTransform != null)
				gogglesBuffer.transform(modelTransform);
			gogglesBuffer.translate(0, headY + 8 / 16f, 0);
			draw(gogglesBuffer, horizontalAngle, ms, solid);
		}

		if (drawHat) {
			SuperByteBuffer hatBuffer = CachedBufferer.partial(AllPartialModels.TRAIN_HAT, blockState);
			if (modelTransform != null)
				hatBuffer.transform(modelTransform);
			hatBuffer.translate(0, headY, 0);
			if (blazeModel == AllPartialModels.BLAZE_INERT) {
				hatBuffer.translateY(0.5f)
					.center()
					.scale(0.75f)
					.uncenter();
			} else {
				hatBuffer.translateY(0.75f);
			}
			hatBuffer
				.rotateCentered(horizontalAngle + Mth.PI, Direction.UP)
				.translate(0.5f, 0, 0.5f)
				.light(LightTexture.FULL_BRIGHT)
				.renderInto(ms, solid);
		}

		if (heatLevel.isAtLeast(HeatLevel.FADING)) {
			PartialModel rodsModel = heatLevel == HeatLevel.SEETHING ? AllPartialModels.BLAZE_BURNER_SUPER_RODS
				: AllPartialModels.BLAZE_BURNER_RODS;
			PartialModel rodsModel2 = heatLevel == HeatLevel.SEETHING ? AllPartialModels.BLAZE_BURNER_SUPER_RODS_2
				: AllPartialModels.BLAZE_BURNER_RODS_2;

			SuperByteBuffer rodsBuffer = CachedBufferer.partial(rodsModel, blockState);
			if (modelTransform != null)
				rodsBuffer.transform(modelTransform);
			rodsBuffer.translate(0, offset1 + animation + .125f, 0)
				.light(LightTexture.FULL_BRIGHT)
				.renderInto(ms, solid);

			SuperByteBuffer rodsBuffer2 = CachedBufferer.partial(rodsModel2, blockState);
			if (modelTransform != null)
				rodsBuffer2.transform(modelTransform);
			rodsBuffer2.translate(0, offset2 + animation - 3 / 16f, 0)
				.light(LightTexture.FULL_BRIGHT)
				.renderInto(ms, solid);
		}

		ms.popPose();
	}

	private static void draw(SuperByteBuffer buffer, float horizontalAngle, PoseStack ms, VertexConsumer vc) {
		buffer.rotateCentered(horizontalAngle, Direction.UP)
			.light(LightTexture.FULL_BRIGHT)
			.renderInto(ms, vc);
	}
}
