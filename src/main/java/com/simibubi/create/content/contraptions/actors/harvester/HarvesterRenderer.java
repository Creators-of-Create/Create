package com.simibubi.create.content.contraptions.actors.harvester;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.math.VecHelper;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class HarvesterRenderer extends SafeBlockEntityRenderer<HarvesterBlockEntity> {

	private static final Vec3 PIVOT = new Vec3(0, 6, 9);

	public HarvesterRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(HarvesterBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		BlockState blockState = be.getBlockState();
		SuperByteBuffer superBuffer = CachedBuffers.partial(AllPartialModels.HARVESTER_BLADE, blockState);
		transform(be.getLevel(), blockState.getValue(HarvesterBlock.FACING), superBuffer, be.getAnimatedSpeed(), PIVOT);
		superBuffer.light(light)
			.renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.cutoutMipped()));
	}

	public static void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffers) {
		BlockState blockState = context.state;
		Direction facing = blockState.getValue(HORIZONTAL_FACING);
		SuperByteBuffer superBuffer = CachedBuffers.partial(AllPartialModels.HARVESTER_BLADE, blockState);
		float speed = (float) (!VecHelper.isVecPointingTowards(context.relativeMotion, facing.getOpposite())
			? context.getAnimationSpeed()
			: 0);
		if (context.contraption.stalled)
			speed = 0;

		superBuffer.transform(matrices.getModel());
		transform(context.world, facing, superBuffer, speed, PIVOT);

		superBuffer.light(LightCoordsUtil.FULL_BRIGHT)
			.useLevelLight(context.world, matrices.getWorld())
			.renderInto(matrices.getViewProjection(), buffers.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.cutoutMipped()));
	}

	public static void transform(Level world, Direction facing, SuperByteBuffer superBuffer, float speed, Vec3 pivot) {
		float originOffset = 1 / 16f;
		Vec3 rotOffset = new Vec3(0, pivot.y * originOffset, pivot.z * originOffset);
		float time = AnimationTickHolder.getRenderTime() / 20;
		float angle = (time * speed) % 360;

		superBuffer.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(facing)), Direction.UP)
			.translate(rotOffset.x, rotOffset.y, rotOffset.z)
			.rotate(AngleHelper.rad(angle), Direction.WEST)
			.translate(-rotOffset.x, -rotOffset.y, -rotOffset.z);
	}
}
