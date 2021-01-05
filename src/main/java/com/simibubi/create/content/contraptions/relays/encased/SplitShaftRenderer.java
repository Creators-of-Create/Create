package com.simibubi.create.content.contraptions.relays.encased;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.render.InstancedBuffer;

import com.simibubi.create.foundation.utility.render.RotatingBuffer;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;

public class SplitShaftRenderer extends KineticTileEntityRenderer {

	public SplitShaftRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		Block block = te.getBlockState().getBlock();
		final Axis boxAxis = ((IRotate) block).getRotationAxis(te.getBlockState());
		final BlockPos pos = te.getPos();

		for (Direction direction : Iterate.directions) {
			Axis axis = direction.getAxis();
			if (boxAxis != axis)
				continue;


			RotatingBuffer shaft = AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(te.getBlockState(), direction);

			shaft.setupInstance(data -> {
				float speed = te.getSpeed();

				float modifier = 1;

				if (te instanceof SplitShaftTileEntity)
					modifier = ((SplitShaftTileEntity) te).getRotationSpeedModifier(direction);

				speed *= modifier;

				data.setPackedLight(light)
					.setRotationalSpeed(speed)
					.setRotationOffset(getRotationOffsetForPosition(te, pos, axis))
					.setRotationAxis(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis).getUnitVector())
					.setPosition(pos);
			});
		}
	}

}
