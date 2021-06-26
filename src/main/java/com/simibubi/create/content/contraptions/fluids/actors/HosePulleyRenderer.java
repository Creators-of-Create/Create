package com.simibubi.create.content.contraptions.fluids.actors;

import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.pulley.AbstractPulleyRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction.Axis;

public class HosePulleyRenderer extends AbstractPulleyRenderer {

	public HosePulleyRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher, AllBlockPartials.HOSE_HALF, AllBlockPartials.HOSE_HALF_MAGNET);
	}

	@Override
	protected Axis getShaftAxis(KineticTileEntity te) {
		return te.getBlockState()
			.get(HosePulleyBlock.HORIZONTAL_FACING)
			.rotateY()
			.getAxis();
	}

	@Override
	protected PartialModel getCoil() {
		return AllBlockPartials.HOSE_COIL;
	}

	@Override
	protected SuperByteBuffer renderRope(KineticTileEntity te) {
		return PartialBufferer.get(AllBlockPartials.HOSE, te.getBlockState());
	}

	@Override
	protected SuperByteBuffer renderMagnet(KineticTileEntity te) {
		return PartialBufferer.get(AllBlockPartials.HOSE_MAGNET, te.getBlockState());
	}

	@Override
	protected float getOffset(KineticTileEntity te, float partialTicks) {
		return ((HosePulleyTileEntity) te).getInterpolatedOffset(partialTicks);
	}

	@Override
	protected boolean isRunning(KineticTileEntity te) {
		return true;
	}

}
