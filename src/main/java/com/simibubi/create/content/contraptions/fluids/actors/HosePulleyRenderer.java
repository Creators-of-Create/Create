package com.simibubi.create.content.contraptions.fluids.actors;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticBlockEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.AbstractPulleyRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction.Axis;

public class HosePulleyRenderer extends AbstractPulleyRenderer {

	public HosePulleyRenderer(BlockEntityRendererProvider.Context context) {
		super(context, AllBlockPartials.HOSE_HALF, AllBlockPartials.HOSE_HALF_MAGNET);
	}

	@Override
	protected Axis getShaftAxis(KineticBlockEntity be) {
		return be.getBlockState()
			.getValue(HosePulleyBlock.HORIZONTAL_FACING)
			.getClockWise()
			.getAxis();
	}

	@Override
	protected PartialModel getCoil() {
		return AllBlockPartials.HOSE_COIL;
	}

	@Override
	protected SuperByteBuffer renderRope(KineticBlockEntity be) {
		return CachedBufferer.partial(AllBlockPartials.HOSE, be.getBlockState());
	}

	@Override
	protected SuperByteBuffer renderMagnet(KineticBlockEntity be) {
		return CachedBufferer.partial(AllBlockPartials.HOSE_MAGNET, be.getBlockState());
	}

	@Override
	protected float getOffset(KineticBlockEntity be, float partialTicks) {
		return ((HosePulleyBlockEntity) be).getInterpolatedOffset(partialTicks);
	}

	@Override
	protected boolean isRunning(KineticBlockEntity be) {
		return true;
	}

}
