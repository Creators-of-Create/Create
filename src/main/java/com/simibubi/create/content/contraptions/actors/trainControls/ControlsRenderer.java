package com.simibubi.create.content.contraptions.actors.trainControls;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class ControlsRenderer {

	public static void render(MovementContext context, VirtualRenderWorld renderWorld, ContraptionMatrices matrices,
		MultiBufferSource buffer, float equipAnimation, float firstLever, float secondLever) {
		BlockState state = context.state;
		Direction facing = state.getValue(ControlsBlock.FACING);

		SuperByteBuffer cover = CachedBufferer.partial(AllPartialModels.TRAIN_CONTROLS_COVER, state);
		float hAngle = 180 + AngleHelper.horizontalAngle(facing);
		PoseStack ms = matrices.getModel();
		cover.transform(ms)
			.center()
			.rotateYDegrees(hAngle)
			.uncenter()
			.light(LevelRenderer.getLightColor(renderWorld, context.localPos))
			.useLevelLight(context.world, matrices.getWorld())
			.renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderType.cutoutMipped()));

		double yOffset = Mth.lerp(equipAnimation * equipAnimation, -0.15f, 0.05f);

		for (boolean first : Iterate.trueAndFalse) {
			float vAngle = Mth.clamp(first ? firstLever * 70 - 25 : secondLever * 15, -45, 45);
			SuperByteBuffer lever = CachedBufferer.partial(AllPartialModels.TRAIN_CONTROLS_LEVER, state);
			ms.pushPose();
			TransformStack.of(ms)
				.center()
				.rotateYDegrees(hAngle)
				.translate(0, 0, 4 / 16f)
				.rotateXDegrees(vAngle - 45)
				.translate(0, yOffset, 0)
				.rotateXDegrees(45)
				.uncenter()
				.translate(0, -2 / 16f, -3 / 16f)
				.translate(first ? 0 : 6 / 16f, 0, 0);
			lever.transform(ms)
				.light(LevelRenderer.getLightColor(renderWorld, context.localPos))
				.useLevelLight(context.world, matrices.getWorld())
				.renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderType.solid()));
			ms.popPose();
		}

	}

}
