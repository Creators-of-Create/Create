package com.simibubi.create.modules.contraptions.components.crank;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Direction;

public class HandCrankTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		super.renderFast(te, x, y, z, partialTicks, destroyStage, buffer);

		BlockState state = te.getBlockState();
		Direction facing = state.get(FACING);
		SuperByteBuffer handle = AllBlockPartials.HAND_CRANK_HANDLE.renderOnDirectional(state, facing.getOpposite());
		HandCrankTileEntity crank = (HandCrankTileEntity) te;
		kineticRotationTransform(handle, te, facing.getAxis(),
				(crank.independentAngle + partialTicks * crank.chasingVelocity) / 360, getWorld());
		handle.translate(x, y, z).renderInto(buffer);
	}

}
