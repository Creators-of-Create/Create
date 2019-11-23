package com.simibubi.create.modules.contraptions.receivers.constructs;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.base.IRotate;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.Vec3d;

public class MechanicalPistonTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderTileEntityFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		super.renderTileEntityFast(te, x, y, z, partialTicks, destroyStage, buffer);

		MechanicalPistonTileEntity pistonTe = (MechanicalPistonTileEntity) te;
		if (!pistonTe.running)
			return;
		Vec3d offset = pistonTe.getConstructOffset(partialTicks).subtract(new Vec3d(pistonTe.getPos()));
		ContraptionRenderer.render(getWorld(), pistonTe.movedContraption, (superBuffer) -> {
			superBuffer.translate(x + offset.x, y + offset.y, z + offset.z);
		}, buffer);
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.SHAFT.block.getDefaultState().with(BlockStateProperties.AXIS,
				((IRotate) te.getBlockState().getBlock()).getRotationAxis(te.getBlockState()));
	}

}
