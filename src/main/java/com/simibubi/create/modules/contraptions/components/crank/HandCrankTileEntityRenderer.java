package com.simibubi.create.modules.contraptions.components.crank;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

public class HandCrankTileEntityRenderer extends KineticTileEntityRenderer {

	public HandCrankTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		BlockState state = te.getBlockState();
		Direction facing = state.get(FACING);
		SuperByteBuffer handle = AllBlockPartials.HAND_CRANK_HANDLE.renderOnDirectional(state, facing.getOpposite());
		HandCrankTileEntity crank = (HandCrankTileEntity) te;
		kineticRotationTransform(handle, te, facing.getAxis(),
				(crank.independentAngle + partialTicks * crank.chasingVelocity) / 360);
		handle.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}

}
