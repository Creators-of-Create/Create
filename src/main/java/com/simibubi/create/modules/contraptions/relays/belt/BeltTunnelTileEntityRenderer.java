package com.simibubi.create.modules.contraptions.relays.belt;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.TileEntityRendererFast;

public class BeltTunnelTileEntityRenderer extends TileEntityRendererFast<BeltTunnelTileEntity> {

	@Override
	public void renderTileEntityFast(BeltTunnelTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		BlockState flapState = AllBlocks.BELT_TUNNEL_FLAP.get().getDefaultState();
		SuperByteBuffer flapBuffer = CreateClient.bufferCache.renderGenericBlockModel(flapState);
		BlockPos pos = te.getPos();
		World world = getWorld();

		for (Direction direction : Direction.values()) {
			if (!te.flaps.containsKey(direction))
				continue;

			float horizontalAngle = direction.getHorizontalAngle() + 90;
			if (direction.getAxis() != Axis.X)
				horizontalAngle += 180;

			float flapPivotX = -15 / 16f;
			float flapPivotY = -.5f;
			float flapPivotZ = 0;
			for (int segment = 0; segment <= 3; segment++) {

				float f = te.flaps.get(direction).get(partialTicks);
				if (direction.getAxis() == Axis.X)
					f *= -1;

				float intensity = segment == 3 ? 1.5f : segment + 1;
				float flapAngle = MathHelper.sin((float) ((1 - Math.abs(f)) * Math.PI * intensity)) * 30 * -f;
				flapAngle = (float) (flapAngle / 180 * Math.PI);

				flapBuffer.translate(0, 0, -segment * 3 / 16f);
				flapBuffer.translate(flapPivotX, flapPivotY, flapPivotZ).rotate(Axis.Z, flapAngle)
						.translate(-flapPivotX, -flapPivotY, -flapPivotZ);
				flapBuffer.rotateCentered(Axis.Y, (float) (horizontalAngle / 180f * Math.PI)).translate(x, y, z);
				flapBuffer.light(te.getBlockState().getPackedLightmapCoords(world, pos)).renderInto(buffer);
			}
		}

	}

}
