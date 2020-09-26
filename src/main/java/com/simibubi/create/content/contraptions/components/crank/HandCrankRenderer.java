package com.simibubi.create.content.contraptions.components.crank;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;

public class HandCrankRenderer extends KineticTileEntityRenderer {

	public HandCrankRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		BlockState state = te.getBlockState();
		Block block = state.getBlock();
		AllBlockPartials renderedHandle = null;
		if (block instanceof HandCrankBlock)
			renderedHandle = ((HandCrankBlock) block).getRenderedHandle();
		if (renderedHandle == null)
			return;

		Direction facing = state.get(FACING);
		SuperByteBuffer handle = renderedHandle.renderOnDirectionalSouth(state, facing.getOpposite());
		HandCrankTileEntity crank = (HandCrankTileEntity) te;
		kineticRotationTransform(handle, te, facing.getAxis(),
			(crank.independentAngle + partialTicks * crank.chasingVelocity) / 360, light);
		handle.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}

}
