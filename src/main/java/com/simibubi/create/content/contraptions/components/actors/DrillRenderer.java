package com.simibubi.create.content.contraptions.components.actors;

import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class DrillRenderer extends KineticBlockEntityRenderer<DrillBlockEntity> {

	public DrillRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(DrillBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
		if (Backend.canUseInstancing(be.getLevel()))
			return;

		BlockState blockState = be.getBlockState();
		renderStationary(be, ms, buffer, light, blockState, AllPartialModels.DRILL_HEAD, AllSpriteShifts.DRILL);
		renderStationary(be, ms, buffer, light, blockState, AllPartialModels.DRILL_HEAD_2, AllSpriteShifts.DRILL_2);
		renderStationary(be, ms, buffer, light, blockState, AllPartialModels.DRILL_HEAD_TOP, AllSpriteShifts.DRILL_TOP);
	}

	protected static void renderStationary(DrillBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light,
		BlockState blockState, PartialModel partial, SpriteShiftEntry spriteShift) {
		SuperByteBuffer superBuffer =
			CachedBufferer.partialFacingVertical(partial, blockState, blockState.getValue(DrillBlock.FACING))
				.light(light);
		applyScroll(be.getLevel(), Math.abs(be.getSpeed()) / 64, spriteShift, superBuffer);
		superBuffer.renderInto(ms, buffer.getBuffer(RenderType.solid()));
	}

	public static void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer) {
		BlockState state = context.state;
		Direction facing = state.getValue(DrillBlock.FACING);
		float speed = (float) Math.min(2.5f, Math.abs(
			context.contraption.stalled || !VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite())
				? context.getAnimationSpeed() / 128
				: 0));
		float time = AnimationTickHolder.getRenderTime() / 20;
		float angle = (float) (((time * speed * 256) % 360));

		SuperByteBuffer superBuffer = CachedBufferer.partial(AllPartialModels.SHAFT_HALF, state);
		superBuffer.transform(matrices.getModel())
			.centre()
			.rotateY(AngleHelper.horizontalAngle(facing) + 180)
			.rotateX(AngleHelper.verticalAngle(facing))
			.rotateZ(angle)
			.unCentre()
			.light(matrices.getWorld(), ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld))
			.renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderType.solid()));

		renderMoving(context, renderWorld, matrices, buffer, state, facing, speed, AllPartialModels.DRILL_HEAD,
			AllSpriteShifts.DRILL);
		renderMoving(context, renderWorld, matrices, buffer, state, facing, speed, AllPartialModels.DRILL_HEAD_2,
			AllSpriteShifts.DRILL_2);
		renderMoving(context, renderWorld, matrices, buffer, state, facing, speed, AllPartialModels.DRILL_HEAD_TOP,
			AllSpriteShifts.DRILL_TOP);
	}

	protected static void renderMoving(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer, BlockState state, Direction facing, float speed,
		PartialModel partial, SpriteShiftEntry spriteShift) {
		SuperByteBuffer superBuffer = CachedBufferer.partial(partial, state);
		superBuffer.transform(matrices.getModel())
			.centre()
			.rotateY(AngleHelper.horizontalAngle(facing))
			.rotateX(AngleHelper.verticalAngle(facing) + 90)
			.unCentre()
			.light(matrices.getWorld(), ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld));
		applyScroll(renderWorld, speed, spriteShift, superBuffer);
		superBuffer.renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderType.solid()));
	}

	public static void applyScroll(Level level, float speed, SpriteShiftEntry spriteShift,
		SuperByteBuffer superBuffer) {
		float spriteSize = spriteShift.getTarget()
			.getV1()
			- spriteShift.getTarget()
				.getV0();
		float time = AnimationTickHolder.getRenderTime(level);
		int frame = (int) (speed * time);
		float scroll = ((frame % 8) / 8f) * spriteSize;
		superBuffer.shiftUVScrolling(spriteShift, scroll);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(DrillBlockEntity be, BlockState state) {
		return CachedBufferer.partialFacing(AllPartialModels.SHAFT_HALF, state, state.getValue(DrillBlock.FACING)
			.getOpposite());
	}

}
