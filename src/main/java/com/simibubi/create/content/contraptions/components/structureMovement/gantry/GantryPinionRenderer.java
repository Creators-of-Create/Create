package com.simibubi.create.content.contraptions.components.structureMovement.gantry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.MatrixStacker;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;

public class GantryPinionRenderer extends KineticTileEntityRenderer {

	public GantryPinionRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		BlockState state = te.getBlockState();
		Direction facing = state.get(GantryPinionBlock.FACING);
		Boolean alongFirst = state.get(GantryPinionBlock.AXIS_ALONG_FIRST_COORDINATE);
		Axis rotationAxis = getRotationAxisOf(te);
		BlockPos visualPos = facing.getAxisDirection() == AxisDirection.POSITIVE ? te.getPos()
			: te.getPos()
				.offset(facing.getOpposite());
		float angleForTe = getAngleForTe(te, visualPos, rotationAxis);

		Axis gantryAxis = Axis.X;
		for (Axis axis : Iterate.axes)
			if (axis != rotationAxis && axis != facing.getAxis())
				gantryAxis = axis;

		if (gantryAxis == Axis.Z)
			if (facing == Direction.DOWN)
				angleForTe *= -1;
		if (gantryAxis == Axis.Y)
			if (facing == Direction.NORTH || facing == Direction.EAST)
				angleForTe *= -1;

		ms.push();

		MatrixStacker msr = MatrixStacker.of(ms);

		msr.centre()
			.rotateY(AngleHelper.horizontalAngle(facing))
			.rotateX(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
			.rotateY(alongFirst ^ facing.getAxis() == Axis.Z ? 90 : 0);

		ms.translate(0, -9 / 16f, 0);
		ms.multiply(Vector3f.POSITIVE_X.getRadialQuaternion(-angleForTe / 2f));
		ms.translate(0, 9 / 16f, 0);

		msr.unCentre();
		AllBlockPartials.GANTRY_COGS.renderOn(state)
			.light(light)
			.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));

		ms.pop();
	}

	@Override
	protected BlockState getRenderedBlockState(KineticTileEntity te) {
		return shaft(getRotationAxisOf(te));
	}

}
