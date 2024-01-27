package com.simibubi.create.content.contraptions.actors.roller;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterRenderer;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RollerRenderer extends SmartBlockEntityRenderer<RollerBlockEntity> {

	public RollerRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(RollerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		BlockState blockState = be.getBlockState();

		ms.pushPose();
		ms.translate(0, -0.25, 0);
		SuperByteBuffer superBuffer = CachedBufferer.partial(AllPartialModels.ROLLER_WHEEL, blockState);
		Direction facing = blockState.getValue(RollerBlock.FACING);
		superBuffer.translate(Vec3.atLowerCornerOf(facing.getNormal())
			.scale(17 / 16f));
		HarvesterRenderer.transform(be.getLevel(), facing, superBuffer, be.getAnimatedSpeed(), Vec3.ZERO);
		superBuffer.translate(0, -.5, .5)
			.rotateYDegrees(90)
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
		ms.popPose();

		CachedBufferer.partial(AllPartialModels.ROLLER_FRAME, blockState)
			.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing) + 180), Direction.UP)
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
	}

	public static void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffers) {
		BlockState blockState = context.state;
		Direction facing = blockState.getValue(HORIZONTAL_FACING);
		SuperByteBuffer superBuffer = CachedBufferer.partial(AllPartialModels.ROLLER_WHEEL, blockState);
		float speed = (float) (!VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite())
			? context.getAnimationSpeed()
			: -context.getAnimationSpeed());
		if (context.contraption.stalled)
			speed = 0;

		superBuffer.transform(matrices.getModel())
			.translate(Vec3.atLowerCornerOf(facing.getNormal())
				.scale(17 / 16f));
		HarvesterRenderer.transform(context.world, facing, superBuffer, speed, Vec3.ZERO);

		PoseStack viewProjection = matrices.getViewProjection();
		viewProjection.pushPose();
		viewProjection.translate(0, -.25, 0);
		int contraptionWorldLight = ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld);
		superBuffer.translate(0, -.5, .5)
			.rotateYDegrees(90)
			.light(matrices.getWorld(), contraptionWorldLight)
			.renderInto(viewProjection, buffers.getBuffer(RenderType.cutoutMipped()));
		viewProjection.popPose();

		CachedBufferer.partial(AllPartialModels.ROLLER_FRAME, blockState)
			.transform(matrices.getModel())
			.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing) + 180), Direction.UP)
			.light(matrices.getWorld(), contraptionWorldLight)
			.renderInto(viewProjection, buffers.getBuffer(RenderType.cutoutMipped()));
	}

}
