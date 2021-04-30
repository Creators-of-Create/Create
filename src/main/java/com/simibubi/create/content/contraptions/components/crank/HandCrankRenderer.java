package com.simibubi.create.content.contraptions.components.crank;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.render.PartialBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.render.backend.FastRenderDispatcher;
import com.simibubi.create.foundation.render.backend.core.PartialModel;

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

		if (FastRenderDispatcher.available(te.getWorld())) return;

		BlockState state = te.getBlockState();
		Block block = state.getBlock();
		PartialModel renderedHandle = null;
		if (block instanceof HandCrankBlock)
			renderedHandle = ((HandCrankBlock) block).getRenderedHandle();
		if (renderedHandle == null)
			return;

		Direction facing = state.get(FACING);
		SuperByteBuffer handle = PartialBufferer.getFacing(renderedHandle, state, facing.getOpposite());
		HandCrankTileEntity crank = (HandCrankTileEntity) te;
		kineticRotationTransform(handle, te, facing.getAxis(),
				(crank.independentAngle + partialTicks * crank.chasingVelocity) / 360, light);
		handle.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}

}
