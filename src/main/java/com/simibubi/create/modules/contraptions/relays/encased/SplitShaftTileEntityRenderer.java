package com.simibubi.create.modules.contraptions.relays.encased;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.base.KineticTileEntity;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;

public class SplitShaftTileEntityRenderer extends KineticTileEntityRenderer {

	@Override
	public void renderTileEntityFast(KineticTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		final Axis boxAxis = te.getBlockState().get(BlockStateProperties.AXIS);
		final BlockState defaultState = AllBlocks.SHAFT_HALF.get().getDefaultState();
		final BlockPos pos = te.getPos();
		float time = AnimationTickHolder.getRenderTick();

		for (Direction direction : Direction.values()) {
			Axis axis = direction.getAxis();
			if (boxAxis != axis)
				continue;

			float offset = getRotationOffsetForPosition(te, pos, axis);
			float angle = (time * te.getSpeed()) % 360;
			float modifier = 1;

			if (te instanceof SplitShaftTileEntity)
				modifier = ((SplitShaftTileEntity) te).getRotationSpeedModifier(direction);

			angle *= modifier;
			angle += offset;
			angle = angle / 180f * (float) Math.PI;

			BlockState state = defaultState.with(BlockStateProperties.FACING, direction);
			SuperByteBuffer superByteBuffer = CreateClient.bufferCache.renderBlockState(KINETIC_TILE, state);
			kineticRotationTransform(superByteBuffer, te, axis, angle, getWorld());
			superByteBuffer.translate(x, y, z).renderInto(buffer);

		}
	}

}
