package com.simibubi.create.content.contraptions.processing.burner;

import javax.annotation.Nullable;

import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.animation.LerpedFloat;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlazeBurnerRenderer extends SafeTileEntityRenderer<BlazeBurnerTileEntity> {

	public BlazeBurnerRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(BlazeBurnerTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		HeatLevel heatLevel = te.getHeatLevelFromBlock();
		if (heatLevel == HeatLevel.NONE)
			return;

		float horizontalAngle = AngleHelper.rad(te.headAngle.getValue(partialTicks));
		Level level = te.getLevel();
		int hashCode = te.hashCode();
		float animation = te.headAnimation.getValue(partialTicks) * .175f;
		BlockState blockState = te.getBlockState();
		boolean drawGoggles = te.goggles;
		boolean drawHat = te.hat;

		renderShared(level, buffer, null, ms, blockState, horizontalAngle, animation, drawGoggles, drawHat, hashCode);
	}

	public static void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer, LerpedFloat headAngle, boolean conductor) {
		BlockState state = context.state;
		if (BlazeBurnerBlock.getHeatLevelOf(state) == HeatLevel.KINDLED)
			state = state.setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.FADING);
		float value = AngleHelper.rad(headAngle.getValue(AnimationTickHolder.getPartialTicks(context.world)));
		renderShared(context.world, buffer, matrices.getModel(), matrices.getViewProjection(), state, value, 0,
			context.tileData.contains("Goggles"), conductor, context.hashCode());
	}

	private static void renderShared(Level level, MultiBufferSource buffer, @Nullable PoseStack modelTransform,
		PoseStack ms, BlockState blockState, float horizontalAngle, float animation, boolean drawGoggles,
		boolean drawHat, int hashCode) {

		HeatLevel heatLevel = BlazeBurnerBlock.getHeatLevelOf(blockState);
		float time = AnimationTickHolder.getRenderTime(level);
		float renderTick = time + (hashCode % 13) * 16f;
		float offsetMult = heatLevel.isAtLeast(HeatLevel.FADING) ? 64 : 16;
		float offset = Mth.sin((float) ((renderTick / 16f) % (2 * Math.PI))) / offsetMult;
		float offset1 = Mth.sin((float) ((renderTick / 16f + Math.PI) % (2 * Math.PI))) / offsetMult;
		float offset2 = Mth.sin((float) ((renderTick / 16f + Math.PI / 2) % (2 * Math.PI))) / offsetMult;

		VertexConsumer solid = buffer.getBuffer(RenderType.solid());
		VertexConsumer cutout = buffer.getBuffer(RenderType.cutoutMipped());

		ms.pushPose();

		if (modelTransform == null && heatLevel.isAtLeast(HeatLevel.FADING)) {
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

			draw(CachedBufferer.partial(AllBlockPartials.BLAZE_BURNER_FLAME, blockState)
				.shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll), horizontalAngle, modelTransform, ms,
				cutout);
		}

		PartialModel blazeModel = heatLevel == HeatLevel.SEETHING ? AllBlockPartials.BLAZE_SUPER
			: heatLevel == HeatLevel.KINDLED ? AllBlockPartials.BLAZE_ACTIVE : AllBlockPartials.BLAZE_IDLE;
		PartialModel rods = heatLevel == HeatLevel.SEETHING ? AllBlockPartials.BLAZE_BURNER_SUPER_RODS
			: AllBlockPartials.BLAZE_BURNER_RODS;
		PartialModel rods2 = heatLevel == HeatLevel.SEETHING ? AllBlockPartials.BLAZE_BURNER_SUPER_RODS_2
			: AllBlockPartials.BLAZE_BURNER_RODS_2;

		float headY = offset - (animation * .75f);

		draw(CachedBufferer.partial(blazeModel, blockState)
			.translate(0, headY, 0), horizontalAngle, modelTransform, ms, solid);

		if (drawGoggles)
			draw(CachedBufferer.partial(AllBlockPartials.BLAZE_GOGGLES, blockState)
				.translate(0, headY + 8 / 16f, 0), horizontalAngle, modelTransform, ms, solid);

		if (drawHat) {
			SuperByteBuffer partial = CachedBufferer.partial(AllBlockPartials.TRAIN_HAT, blockState);
			if (modelTransform != null)
				partial.transform(modelTransform);
			partial.translate(0, headY + 0.75f, 0)
				.rotateCentered(Direction.UP, horizontalAngle + Mth.PI)
				.translate(0.5f, 0, 0.5f)
				.light(LightTexture.FULL_BRIGHT)
				.renderInto(ms, solid);
		}

		draw(CachedBufferer.partial(rods, blockState)
			.translate(0, offset1 + animation + .125f, 0), 0, modelTransform, ms, solid);
		draw(CachedBufferer.partial(rods2, blockState)
			.translate(0, offset2 + animation - 3 / 16f, 0), 0, modelTransform, ms, solid);

		ms.popPose();
	}

	private static void draw(SuperByteBuffer blazeBuffer, float horizontalAngle, @Nullable PoseStack modelTransform,
		PoseStack ms, VertexConsumer vb) {
		if (modelTransform != null)
			blazeBuffer.transform(modelTransform);
		blazeBuffer.rotateCentered(Direction.UP, horizontalAngle)
			.light(LightTexture.FULL_BRIGHT)
			.renderInto(ms, vb);
	}
}
