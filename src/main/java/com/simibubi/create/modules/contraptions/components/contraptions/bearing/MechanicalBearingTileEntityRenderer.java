package com.simibubi.create.modules.contraptions.components.contraptions.bearing;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

public class MechanicalBearingTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		super.renderFast(te, x, y, z, partialTicks, destroyStage, buffer);

		MechanicalBearingTileEntity bearingTe = (MechanicalBearingTileEntity) te;
		final Direction facing = te.getBlockState().get(BlockStateProperties.FACING);
		SuperByteBuffer superBuffer = AllBlockPartials.MECHANICAL_BEARING_TOP.renderOn(te.getBlockState());
		superBuffer.rotateCentered(Axis.X, AngleHelper.rad(-90 - AngleHelper.verticalAngle(facing)));
		if (facing.getAxis().isHorizontal())
			superBuffer.rotateCentered(Axis.Y, AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())));
		float interpolatedAngle = bearingTe.getInterpolatedAngle(partialTicks);
		kineticRotationTransform(superBuffer, bearingTe, facing.getAxis(), (float) (interpolatedAngle / 180 * Math.PI),
				getWorld());
		superBuffer.translate(x, y, z).renderInto(buffer);
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return AllBlockPartials.SHAFT_HALF.renderOnDirectional(te.getBlockState(),
				te.getBlockState().get(MechanicalBearingBlock.FACING).getOpposite());
	}

}
