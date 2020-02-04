package com.simibubi.create.modules.contraptions.relays.gearbox;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;

public class GearboxTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		final Axis boxAxis = te.getBlockState().get(BlockStateProperties.AXIS);
		final BlockPos pos = te.getPos();
		float time = AnimationTickHolder.getRenderTick();

		for (Direction direction : Direction.values()) {
			final Axis axis = direction.getAxis();
			if (boxAxis == axis)
				continue;

			SuperByteBuffer shaft = AllBlockPartials.SHAFT_HALF.renderOnDirectional(te.getBlockState(), direction);
			float offset = getRotationOffsetForPosition(te, pos, axis);
			float angle = (time * te.getSpeed() * 3f / 10) % 360;

			if (te.getSpeed() != 0 && te.hasSource()) {
				BlockPos source = te.getSource().subtract(te.getPos());
				Direction sourceFacing = Direction.getFacingFromVector(source.getX(), source.getY(), source.getZ());
				if (sourceFacing.getAxis() == direction.getAxis())
					angle *= sourceFacing == direction ? 1 : -1;
				else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
					angle *= -1;
			}

			angle += offset;
			angle = angle / 180f * (float) Math.PI;

			kineticRotationTransform(shaft, te, axis, angle, getWorld());
			shaft.translate(x, y, z).renderInto(buffer);
		}
	}

}
