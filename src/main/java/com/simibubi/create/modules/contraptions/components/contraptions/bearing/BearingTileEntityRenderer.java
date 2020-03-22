package com.simibubi.create.modules.contraptions.components.contraptions.bearing;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;

public class BearingTileEntityRenderer extends KineticTileEntityRenderer {

	public BearingTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);

		IBearingTileEntity bearingTe = (IBearingTileEntity) te;
		final Direction facing = te.getBlockState().get(BlockStateProperties.FACING);
		SuperByteBuffer superBuffer = AllBlockPartials.MECHANICAL_BEARING_TOP.renderOn(te.getBlockState());
		superBuffer.rotateCentered(Axis.X, AngleHelper.rad(-90 - AngleHelper.verticalAngle(facing)));
		if (facing.getAxis().isHorizontal())
			superBuffer.rotateCentered(Axis.Y, AngleHelper.rad(AngleHelper.horizontalAngle(facing.getOpposite())));
		float interpolatedAngle = bearingTe.getInterpolatedAngle(partialTicks - 1);
		kineticRotationTransform(superBuffer, te, facing.getAxis(), (float) (interpolatedAngle / 180 * Math.PI));
		superBuffer.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}

	@Override
	protected SuperByteBuffer getRotatedModel(KineticTileEntity te) {
		return AllBlockPartials.SHAFT_HALF.renderOnDirectional(te.getBlockState(),
				te.getBlockState().get(BearingBlock.FACING).getOpposite());
	}

}
