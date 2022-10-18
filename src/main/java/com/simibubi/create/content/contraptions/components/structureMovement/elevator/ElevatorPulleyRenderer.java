package com.simibubi.create.content.contraptions.components.structureMovement.elevator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllSpriteShifts;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.AbstractPulleyRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyRenderer;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.PulleyTileEntity;
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

public class ElevatorPulleyRenderer extends KineticTileEntityRenderer {

	public ElevatorPulleyRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {

//		if (Backend.canUseInstancing(te.getLevel()))
//			return;

		// from KTE. replace with super call when flw instance is implemented
		BlockState state = getRenderedBlockState(te);
		RenderType type = getRenderType(te, state);
		if (type != null)
			renderRotatingBuffer(te, getRotatedModel(te, state), ms, buffer.getBuffer(type), light);
		//
		
		float offset = PulleyRenderer.getTileOffset(partialTicks, (PulleyTileEntity) te);
		boolean running = PulleyRenderer.isPulleyRunning(te);

		SpriteShiftEntry beltShift = AllSpriteShifts.ELEVATOR_BELT;
		SpriteShiftEntry coilShift = AllSpriteShifts.ELEVATOR_COIL;
		VertexConsumer vb = buffer.getBuffer(RenderType.solid());
		Level world = te.getLevel();
		BlockState blockState = te.getBlockState();
		BlockPos pos = te.getBlockPos();

		SuperByteBuffer magnet = CachedBufferer.partial(AllBlockPartials.ELEVATOR_MAGNET, blockState);
		if (running || offset == 0)
			AbstractPulleyRenderer.renderAt(world, magnet, offset, pos, ms, vb);

		SuperByteBuffer rotatedCoil = getRotatedCoil(te);
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

		SuperByteBuffer halfRope = CachedBufferer.partial(AllBlockPartials.ELEVATOR_BELT_HALF, blockState);
		SuperByteBuffer rope = CachedBufferer.partial(AllBlockPartials.ELEVATOR_BELT, blockState);

		float f = offset % 1;
		if (f < .25f || f > .75f) {
			halfRope.centre()
				.rotateY(180 + AngleHelper.horizontalAngle(blockState.getValue(ElevatorPulleyBlock.HORIZONTAL_FACING)))
				.unCentre();
			AbstractPulleyRenderer.renderAt(world,
				halfRope.shiftUVScrolling(beltShift, (float) beltScroll * spriteSize), f > .75f ? f - 1 : f, pos, ms,
				vb);
		}

		if (!running)
			return;

		for (int i = 0; i < offset - .25f; i++) {
			rope.centre()
				.rotateY(180 + AngleHelper.horizontalAngle(blockState.getValue(ElevatorPulleyBlock.HORIZONTAL_FACING)))
				.unCentre();
			AbstractPulleyRenderer.renderAt(world, rope.shiftUVScrolling(beltShift, (float) beltScroll * spriteSize),
				offset - i, pos, ms, vb);
		}
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return shaft(getRotationAxisOf(te));
	}

	protected SuperByteBuffer getRotatedCoil(KineticTileEntity te) {
		BlockState blockState = te.getBlockState();
		return CachedBufferer.partialFacing(AllBlockPartials.ELEVATOR_COIL, blockState,
			blockState.getValue(ElevatorPulleyBlock.HORIZONTAL_FACING));
	}

	@Override
	public int getViewDistance() {
		return 128;
	}

	@Override
	public boolean shouldRenderOffScreen(KineticTileEntity p_188185_1_) {
		return true;
	}

}
