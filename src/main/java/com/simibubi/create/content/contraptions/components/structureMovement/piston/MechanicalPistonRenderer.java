package com.simibubi.create.content.contraptions.components.structureMovement.piston;

import com.simibubi.create.content.contraptions.base.KineticBlockEntityRenderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class MechanicalPistonRenderer extends KineticBlockEntityRenderer<MechanicalPistonBlockEntity> {

	public MechanicalPistonRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected BlockState getRenderedBlockState(MechanicalPistonBlockEntity be) {
		return shaft(getRotationAxisOf(be));
	}

}
