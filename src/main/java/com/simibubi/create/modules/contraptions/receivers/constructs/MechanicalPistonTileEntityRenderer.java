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

		// SPECIAL RENDER
		MechanicalPistonTileEntity pistonTe = (MechanicalPistonTileEntity) te;

		if (!pistonTe.running)
			return;

		ContraptionRenderer.cacheContraptionIfMissing(pistonTe.movedContraption);
		renderConstructFromCache(pistonTe.movedContraption, pistonTe, x, y, z, partialTicks, buffer);

		Vec3d offset = pistonTe.getConstructOffset(partialTicks).subtract(new Vec3d(pistonTe.getPos()));
		ContraptionRenderer.renderActors(pistonTe.getWorld(), pistonTe.movedContraption, (float) (x + offset.x),
				(float) (y + offset.y), (float) (z + offset.z), 0, 0, buffer);
	}

	protected void renderConstructFromCache(Contraption c, MechanicalPistonTileEntity te, double x, double y, double z,
			float partialTicks, BufferBuilder buffer) {
		final Vec3d offset = te.getConstructOffset(partialTicks);
		float xPos = (float) (x - te.getPos().getX());
		float yPos = (float) (y - te.getPos().getY());
		float zPos = (float) (z - te.getPos().getZ());
		buffer.putBulkData(ContraptionRenderer.get(c).getTranslated(te.getWorld(), xPos, yPos, zPos, offset));
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return AllBlocks.SHAFT.block.getDefaultState().with(BlockStateProperties.AXIS,
				((IRotate) te.getBlockState().getBlock()).getRotationAxis(te.getBlockState()));
	}

}
