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
		return isPulleyRunning(te);
	}
	
	public static boolean isPulleyRunning(KineticTileEntity te) {
		PulleyTileEntity pte = (PulleyTileEntity) te;
		return pte.running || pte.mirrorParent != null || te.isVirtual();
	}

	public static float getTileOffset(float partialTicks, PulleyTileEntity tile) {
		float offset = tile.getInterpolatedOffset(partialTicks);

		AbstractContraptionEntity attachedContraption = tile.getAttachedContraption();
		if (attachedContraption != null) {
			PulleyContraption c = (PulleyContraption) attachedContraption.getContraption();
			double entityPos = Mth.lerp(partialTicks, attachedContraption.yOld, attachedContraption.getY());
			offset = (float) -(entityPos - c.anchor.getY() - c.getInitialOffset());
		}

		return offset;
	}
	
	@Override
	public int getViewDistance() {
		return 128;
	}
	
}
