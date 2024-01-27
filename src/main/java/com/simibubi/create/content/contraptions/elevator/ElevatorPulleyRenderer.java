package com.simibubi.create.content.contraptions.elevator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.pulley.AbstractPulleyRenderer;
import com.simibubi.create.content.contraptions.pulley.PulleyRenderer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.block.render.SpriteShiftEntry;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ElevatorPulleyRenderer extends KineticBlockEntityRenderer<ElevatorPulleyBlockEntity> {

	public ElevatorPulleyRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(ElevatorPulleyBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {

//		if (VisualizationManager.supportsVisualization(be.getLevel()))
//			return;

		// from KBE. replace with super call when flw instance is implemented
		BlockState state = getRenderedBlockState(be);
		RenderType type = getRenderType(be, state);
		if (type != null)
			renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(type), light);
		//

		float offset = PulleyRenderer.getBlockEntityOffset(partialTicks, be);
		boolean running = PulleyRenderer.isPulleyRunning(be);

		SpriteShiftEntry beltShift = AllSpriteShifts.ELEVATOR_BELT;
		SpriteShiftEntry coilShift = AllSpriteShifts.ELEVATOR_COIL;
		VertexConsumer vb = buffer.getBuffer(RenderType.solid());
		Level world = be.getLevel();
		BlockState blockState = be.getBlockState();
		BlockPos pos = be.getBlockPos();

		float blockStateAngle =
			180 + AngleHelper.horizontalAngle(blockState.getValue(ElevatorPulleyBlock.HORIZONTAL_FACING));

		SuperByteBuffer magnet = CachedBufferer.partial(AllPartialModels.ELEVATOR_MAGNET, blockState);
		if (running || offset == 0)
			AbstractPulleyRenderer.renderAt(world, magnet.center()
				.rotateYDegrees(blockStateAngle)
				.uncenter(), offset, pos, ms, vb);

		SuperByteBuffer rotatedCoil = getRotatedCoil(be);
		if (offset == 0) {
			rotatedCoil.light(light)
				.renderInto(ms, vb);
			return;
		}

		float spriteSize = beltShift.getTarget()
			.getV1()
			- beltShift.getTarget()
				.getV0();

		double coilScroll = -(offset + 3 / 16f) - Math.floor((offset + 3 / 16f) * -2) / 2;
		double beltScroll = (-(offset + .5) - Math.floor(-(offset + .5))) / 2;

		rotatedCoil.shiftUVScrolling(coilShift, (float) coilScroll * spriteSize)
			.light(light)
			.renderInto(ms, vb);

		SuperByteBuffer halfRope = CachedBufferer.partial(AllPartialModels.ELEVATOR_BELT_HALF, blockState);
		SuperByteBuffer rope = CachedBufferer.partial(AllPartialModels.ELEVATOR_BELT, blockState);

		float f = offset % 1;
		if (f < .25f || f > .75f) {
			halfRope.center()
				.rotateYDegrees(blockStateAngle)
				.uncenter();
			AbstractPulleyRenderer.renderAt(world,
				halfRope.shiftUVScrolling(beltShift, (float) beltScroll * spriteSize), f > .75f ? f - 1 : f, pos, ms,
				vb);
		}

		if (!running)
			return;

		for (int i = 0; i < offset - .25f; i++) {
			rope.center()
				.rotateYDegrees(blockStateAngle)
				.uncenter();
			AbstractPulleyRenderer.renderAt(world, rope.shiftUVScrolling(beltShift, (float) beltScroll * spriteSize),
				offset - i, pos, ms, vb);
		}
	}

	@Override
	protected BlockState getRenderedBlockState(ElevatorPulleyBlockEntity be) {
		return shaft(getRotationAxisOf(be));
	}

	protected SuperByteBuffer getRotatedCoil(KineticBlockEntity be) {
		BlockState blockState = be.getBlockState();
		return CachedBufferer.partialFacing(AllPartialModels.ELEVATOR_COIL, blockState,
			blockState.getValue(ElevatorPulleyBlock.HORIZONTAL_FACING));
	}

	@Override
	public int getViewDistance() {
		return 128;
	}

	@Override
	public boolean shouldRenderOffScreen(ElevatorPulleyBlockEntity p_188185_1_) {
		return true;
	}

}
