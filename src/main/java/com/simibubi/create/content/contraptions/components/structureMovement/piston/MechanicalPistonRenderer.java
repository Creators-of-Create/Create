package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;

public class MechanicalPistonRenderer extends KineticTileEntityRenderer {

	public MechanicalPistonRenderer(BlockEntityRendererProvider.Context dispatcher) {
		super(dispatcher);
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return shaft(getRotationAxisOf(te));
	}

}
