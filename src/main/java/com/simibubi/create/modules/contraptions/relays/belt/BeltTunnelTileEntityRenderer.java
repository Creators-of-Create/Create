package com.simibubi.create.modules.contraptions.relays.belt;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.modules.contraptions.relays.belt.BeltTunnelBlock.Shape;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.TileEntityRendererFast;

public class BeltTunnelTileEntityRenderer extends TileEntityRendererFast<BeltTunnelTileEntity> {

	@Override
	public void renderTileEntityFast(BeltTunnelTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		BlockState flapState = AllBlocks.BELT_TUNNEL_FLAP.get().getDefaultState();
		BlockState tunnelState = te.getBlockState();
		SuperByteBuffer flapBuffer = CreateClient.bufferCache.renderGenericBlockModel(flapState);
		BlockPos pos = te.getPos();
		World world = getWorld();

		for (Direction direction : Direction.values()) {
			// TODO: move flap presence to TE
			if (direction.getAxis().isVertical())
				continue;
			if (AllBlocks.BELT_TUNNEL.typeOf(world.getBlockState(pos.offset(direction))))
				continue;
			if (direction.getAxis() != tunnelState.get(BlockStateProperties.HORIZONTAL_AXIS)) {
				boolean positive = direction.getAxisDirection() == AxisDirection.POSITIVE;
				Shape shape = tunnelState.get(BeltTunnelBlock.SHAPE);
				if (shape == Shape.STRAIGHT || shape == Shape.WINDOW)
					continue;
				if (positive && shape == Shape.T_LEFT)
					continue;
				if (!positive && shape == Shape.T_RIGHT)
					continue;
			}

			// get flap angle

			flapBuffer.rotateCentered(Axis.Y, (float) (direction.getHorizontalAngle() / 180f * Math.PI));
			flapBuffer.translate(x, y, z);
			flapBuffer.light(te.getBlockState().getPackedLightmapCoords(world, pos)).renderInto(buffer);
		}

	}

}
