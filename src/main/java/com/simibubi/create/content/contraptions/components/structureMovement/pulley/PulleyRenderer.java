package com.simibubi.create.content.contraptions.components.structureMovement.pulley;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
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
	protected Axis getShaftAxis(KineticBlockEntity be) {
		return be.getBlockState()
			.getValue(PulleyBlock.HORIZONTAL_AXIS);
	}

	@Override
	protected PartialModel getCoil() {
		return AllBlockPartials.ROPE_COIL;
	}

	@Override
	protected SuperByteBuffer renderRope(KineticBlockEntity be) {
		return CachedBufferer.block(AllBlocks.ROPE.getDefaultState());
	}

	@Override
	protected SuperByteBuffer renderMagnet(KineticBlockEntity be) {
		return CachedBufferer.block(AllBlocks.PULLEY_MAGNET.getDefaultState());
	}

	@Override
	protected float getOffset(KineticBlockEntity be, float partialTicks) {
		PulleyBlockEntity pulley = (PulleyBlockEntity) be;
		return getBlockEntityOffset(partialTicks, pulley);
	}

	@Override
	protected boolean isRunning(KineticBlockEntity be) {
		return isPulleyRunning(be);
	}
	
	public static boolean isPulleyRunning(KineticBlockEntity be) {
		PulleyBlockEntity pte = (PulleyBlockEntity) be;
		return pte.running || pte.mirrorParent != null || be.isVirtual();
	}

	public static float getBlockEntityOffset(float partialTicks, PulleyBlockEntity blockEntity) {
		float offset = blockEntity.getInterpolatedOffset(partialTicks);

		AbstractContraptionEntity attachedContraption = blockEntity.getAttachedContraption();
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
