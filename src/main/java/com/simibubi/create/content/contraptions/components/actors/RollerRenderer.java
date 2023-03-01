package com.simibubi.create.content.contraptions.components.actors;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RollerRenderer extends SmartBlockEntityRenderer<RollerBlockEntity> {

	private static final Vec3 PIVOT = new Vec3(0, -4, 16);

	public RollerRenderer(Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(RollerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		BlockState blockState = be.getBlockState();

		ms.pushPose();
		ms.translate(0, -.25, 0);
		SuperByteBuffer superBuffer = CachedBufferer.partial(AllPartialModels.ROLLER_WHEEL, blockState)
			.translate(0, .25, 0);
		Direction facing = blockState.getValue(RollerBlock.FACING);
		HarvesterRenderer.transform(be.getLevel(), facing, superBuffer, be.getAnimatedSpeed(), PIVOT);
		superBuffer.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
		ms.popPose();

		CachedBufferer.partial(AllPartialModels.ROLLER_FRAME, blockState)
			.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing)))
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.solid()));
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

		superBuffer.translate(0, .25, 0)
			.transform(matrices.getModel());
		HarvesterRenderer.transform(context.world, facing, superBuffer, speed, PIVOT);

		PoseStack viewProjection = matrices.getViewProjection();
		viewProjection.pushPose();
		viewProjection.translate(0, -.25, 0);
		int contraptionWorldLight = ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld);
		superBuffer.light(matrices.getWorld(), contraptionWorldLight)
			.renderInto(viewProjection, buffers.getBuffer(RenderType.cutoutMipped()));
		viewProjection.popPose();

		CachedBufferer.partial(AllPartialModels.ROLLER_FRAME, blockState)
			.transform(matrices.getModel())
			.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing)))
			.light(matrices.getWorld(), contraptionWorldLight)
			.renderInto(viewProjection, buffers.getBuffer(RenderType.solid()));
	}

}
