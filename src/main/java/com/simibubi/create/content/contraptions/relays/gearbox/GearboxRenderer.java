package com.simibubi.create.content.contraptions.relays.gearbox;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.base.KineticTileEntityRenderer;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.render.InstancedBuffer;

import com.simibubi.create.foundation.utility.render.RotatingBuffer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;

public class GearboxRenderer extends KineticTileEntityRenderer {

	public GearboxRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		final Axis boxAxis = te.getBlockState().get(BlockStateProperties.AXIS);
		final BlockPos pos = te.getPos();

		for (Direction direction : Iterate.directions) {
			final Axis axis = direction.getAxis();
			if (boxAxis == axis)
				continue;

			RotatingBuffer shaft = AllBlockPartials.SHAFT_HALF.renderOnDirectionalSouthRotating(te.getBlockState(), direction);

			shaft.setupInstance(data -> {
				float speed = te.getSpeed();

				if (te.getSpeed() != 0 && te.hasSource()) {
					BlockPos source = te.source.subtract(te.getPos());
					Direction sourceFacing = Direction.getFacingFromVector(source.getX(), source.getY(), source.getZ());
					if (sourceFacing.getAxis() == direction.getAxis())
						speed *= sourceFacing == direction ? 1 : -1;
					else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
						speed *= -1;
				}

				data.setPackedLight(light)
					.setRotationalSpeed(speed)
					.setRotationOffset(getRotationOffsetForPosition(te, pos, axis))
					.setRotationAxis(Direction.getFacingFromAxis(Direction.AxisDirection.POSITIVE, axis).getUnitVector())
					.setPosition(pos);
			});
		}
	}

}
