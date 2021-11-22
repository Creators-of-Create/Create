package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;

public class PulleyRenderer extends AbstractPulleyRenderer {

	public PulleyRenderer(BlockEntityRendererProvider.Context context) {
		super(context, AllBlockPartials.ROPE_HALF, AllBlockPartials.ROPE_HALF_MAGNET);
	}

	@Override
	protected Axis getShaftAxis(KineticTileEntity te) {
		return te.getBlockState()
			.getValue(PulleyBlock.HORIZONTAL_AXIS);
	}

	@Override
	protected PartialModel getCoil() {
		return AllBlockPartials.ROPE_COIL;
	}

	@Override
	protected SuperByteBuffer renderRope(KineticTileEntity te) {
		return CachedBufferer.block(AllBlocks.ROPE.getDefaultState());
	}

	@Override
	protected SuperByteBuffer renderMagnet(KineticTileEntity te) {
		return CachedBufferer.block(AllBlocks.PULLEY_MAGNET.getDefaultState());
	}

	@Override
	protected float getOffset(KineticTileEntity te, float partialTicks) {
		PulleyTileEntity pulley = (PulleyTileEntity) te;
		return getTileOffset(partialTicks, pulley);
	}

	@Override
	protected boolean isRunning(KineticTileEntity te) {
		return ((PulleyTileEntity) te).running || te.isVirtual();
	}


	static float getTileOffset(float partialTicks, PulleyTileEntity tile) {
		float offset = tile.getInterpolatedOffset(partialTicks);

		if (tile.movedContraption != null) {
			AbstractContraptionEntity e = tile.movedContraption;
			PulleyContraption c = (PulleyContraption) tile.movedContraption.getContraption();
			double entityPos = Mth.lerp(partialTicks, e.yOld, e.getY());
			offset = (float) -(entityPos - c.anchor.getY() - c.initialOffset);
		}

		return offset;
	}
	
	@Override
	public int getViewDistance() {
		return 128;
	}
	
}
