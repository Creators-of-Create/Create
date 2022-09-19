package com.simibubi.create.content.contraptions.components.actors;

import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.components.structureMovement.render.ContraptionRenderDispatcher;
import com.simibubi.create.foundation.render.CachedPartialBuffers;
import com.simibubi.create.foundation.render.FlwSuperByteBuffer;

import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.catnip.utility.math.AngleHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class DrillRenderer extends KineticTileEntityRenderer {

	public DrillRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te, BlockState state) {
		return CachedPartialBuffers.partialFacing(AllBlockPartials.DRILL_HEAD, state);
	}

	public static void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
		ContraptionMatrices matrices, MultiBufferSource buffer) {
		BlockState state = context.state;
		SuperByteBuffer superBuffer = CachedPartialBuffers.partial(AllBlockPartials.DRILL_HEAD, state);
		Direction facing = state.getValue(DrillBlock.FACING);

		float speed = (float) (context.contraption.stalled
				|| !VecHelper.isVecPointingTowards(context.relativeMotion, facing
				.getOpposite()) ? context.getAnimationSpeed() : 0);
		float time = AnimationTickHolder.getRenderTime() / 20;
		float angle = (float) (((time * speed) % 360));

		FlwSuperByteBuffer.cast(superBuffer).ifPresent(superByteBuffer -> {
			superByteBuffer
					.transform(matrices.getModel())
					.centre()
					.rotateY(AngleHelper.horizontalAngle(facing))
					.rotateX(AngleHelper.verticalAngle(facing))
					.rotateZ(angle)
					.unCentre()
					.light(matrices.getWorld(),
							ContraptionRenderDispatcher.getContraptionWorldLight(context, renderWorld))
					.renderInto(matrices.getViewProjection(), buffer.getBuffer(RenderType.solid()));
		});

	}

}
