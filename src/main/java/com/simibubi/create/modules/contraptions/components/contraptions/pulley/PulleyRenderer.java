package com.simibubi.create.modules.contraptions.components.contraptions.pulley;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.modules.contraptions.components.contraptions.ContraptionEntity;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class PulleyRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderFast(KineticTileEntity te, double x, double y, double z, float partialTicks, int destroyStage,
			BufferBuilder buffer) {
		super.renderFast(te, x, y, z, partialTicks, destroyStage, buffer);

		PulleyTileEntity pulley = (PulleyTileEntity) te;
		BlockState blockState = te.getBlockState();
		BlockPos pos = te.getPos();

		SuperByteBuffer halfMagnet = AllBlockPartials.ROPE_HALF_MAGNET.renderOn(blockState);
		SuperByteBuffer halfRope = AllBlockPartials.ROPE_HALF.renderOn(blockState);
		SuperByteBuffer magnet = CreateClient.bufferCache.renderBlock(AllBlocks.PULLEY_MAGNET.getDefault());
		SuperByteBuffer rope = CreateClient.bufferCache.renderBlock(AllBlocks.ROPE.getDefault());

		boolean moving = pulley.running && (pulley.movedContraption == null || !pulley.movedContraption.isStalled());
		float offset = pulley.getInterpolatedOffset(moving ? partialTicks : 0.5f);
		
		if (pulley.movedContraption != null) {
			ContraptionEntity e = pulley.movedContraption;
			PulleyContraption c = (PulleyContraption) pulley.movedContraption.getContraption();
			double entityPos = MathHelper.lerp(partialTicks, e.lastTickPosY, e.posY);
			offset = (float) -(entityPos - c.getAnchor().getY() - c.initialOffset);
		}
		
		if (pulley.running || pulley.offset == 0)
			renderAt(offset > .25f ? magnet : halfMagnet, x, y, z, offset, pos, buffer);

		float f = offset % 1;
		if (offset > .75f && (f < .25f || f > .75f))
			renderAt(halfRope, x, y, z, f > .75f ? f - 1 : f, pos, buffer);

		if (!pulley.running)
			return;

		for (int i = 0; i < offset - 1.25f; i++)
			renderAt(rope, x, y, z, offset - i - 1, pos, buffer);
	}

	public void renderAt(SuperByteBuffer partial, double x, double y, double z, float offset, BlockPos pulleyPos,
			BufferBuilder buffer) {
		BlockPos actualPos = pulleyPos.down((int) offset);
		int light = getWorld().getBlockState(actualPos).getPackedLightmapCoords(getWorld(), actualPos);
		partial.translate(x, y - offset, z).light(light).renderInto(buffer);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		BlockState blockState = te.getBlockState();
		return AllBlockPartials.ROPE_COIL.renderOnDirectional(blockState, horizontalFacing(blockState));
	}

	public Direction horizontalFacing(BlockState blockState) {
		return Direction.getFacingFromAxis(AxisDirection.POSITIVE, blockState.get(PulleyBlock.HORIZONTAL_AXIS));
	}

}
