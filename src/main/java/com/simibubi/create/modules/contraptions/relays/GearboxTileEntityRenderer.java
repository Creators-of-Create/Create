package com.simibubi.create.modules.contraptions.relays;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.animation.Animation;

public class GearboxTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderTileEntityFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		final Axis boxAxis = te.getBlockState().get(BlockStateProperties.AXIS);
		final BlockPos pos = te.getPos();
		float time = Animation.getWorldTime(Minecraft.getInstance().world, partialTicks);
		final BlockState defaultState = AllBlocks.SHAFT_HALF.get().getDefaultState();

		for (Direction direction : Direction.values()) {
			final Axis axis = direction.getAxis();
			if (boxAxis == axis)
				continue;
			if (AllBlocks.GEARBOX.typeOf(getWorld().getBlockState(pos.offset(direction))))
				continue;

			BlockState state = defaultState.with(BlockStateProperties.FACING, direction);
			cacheIfMissing(state, BlockModelSpinner::new);

			float offset = getRotationOffsetForPosition(te, pos, axis);
			float angle = (time * te.getSpeed()) % 360;

			if (te.getSpeed() != 0) {
				BlockPos source = te.getSource().subtract(te.getPos());
				Direction sourceFacing = Direction.getFacingFromVector(source.getX(), source.getY(), source.getZ());
				if (sourceFacing.getAxis() == direction.getAxis())
					angle *= sourceFacing == direction ? 1 : -1;
				else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
					angle *= -1;
			}

			angle += offset;
			angle = angle / 180f * (float) Math.PI;

			renderFromCache(buffer, state, (float) x, (float) y, (float) z, pos, axis, angle);
		}
	}

}
