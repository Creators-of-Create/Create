package com.simibubi.create.content.contraptions.relays.encased;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;

public class ShaftlessCogRenderer extends KineticTileEntityRenderer {

	private boolean large;

	public static ShaftlessCogRenderer small(BlockEntityRendererProvider.Context context) {
		return new ShaftlessCogRenderer(context, false);
	}

	public static ShaftlessCogRenderer large(BlockEntityRendererProvider.Context context) {
		return new ShaftlessCogRenderer(context, true);
	}

	public ShaftlessCogRenderer(BlockEntityRendererProvider.Context context, boolean large) {
		super(context);
		this.large = large;
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return PartialBufferer.getFacingVertical(
			large ? AllBlockPartials.SHAFTLESS_LARGE_COGWHEEL : AllBlockPartials.SHAFTLESS_COGWHEEL, te.getBlockState(),
			Direction.fromAxisAndDirection(te.getBlockState()
				.getValue(EncasedCogwheelBlock.AXIS), AxisDirection.POSITIVE));
	}

}
