package com.simibubi.create.content.contraptions.actors.harvester;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.math.AngleHelper;
import net.createmod.ponder.utility.LevelTickHolder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
			.renderInto(ms, buffer.getBuffer(RenderType.cutoutMipped()));
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

		superBuffer
			.light(matrices.getWorld(), ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld))
			.renderInto(matrices.getViewProjection(), buffers.getBuffer(RenderType.cutoutMipped()));
	}

	public static void transform(Level world, Direction facing, SuperByteBuffer superBuffer, float speed, Vec3 pivot) {
		float originOffset = 1 / 16f;
		Vec3 rotOffset = new Vec3(0, pivot.y * originOffset, pivot.z * originOffset);
		float time = LevelTickHolder.getRenderTime(world) / 20;
		float angle = (time * speed) % 360;

		superBuffer.rotateCentered(Direction.UP, AngleHelper.rad(AngleHelper.horizontalAngle(facing)))
			.translate(rotOffset.x, rotOffset.y, rotOffset.z)
			.rotate(Direction.WEST, AngleHelper.rad(angle))
			.translate(-rotOffset.x, -rotOffset.y, -rotOffset.z);
	}
}
