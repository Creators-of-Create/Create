package com.simibubi.create.modules.contraptions.relays.gearbox;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;

public class GearboxTileEntityRenderer extends KineticTileEntityRenderer {

	public GearboxTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(KineticTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
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
				BlockPos source = te.source.subtract(te.getPos());
				Direction sourceFacing = Direction.getFacingFromVector(source.getX(), source.getY(), source.getZ());
				if (sourceFacing.getAxis() == direction.getAxis())
					angle *= sourceFacing == direction ? 1 : -1;
				else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
					angle *= -1;
			}

			angle += offset;
			angle = angle / 180f * (float) Math.PI;

			kineticRotationTransform(shaft, te, axis, angle);
			shaft.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
		}
	}

}
