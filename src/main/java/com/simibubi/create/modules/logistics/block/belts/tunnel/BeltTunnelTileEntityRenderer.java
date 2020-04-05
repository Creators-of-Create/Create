package com.simibubi.create.modules.logistics.block.belts.tunnel;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.block.SafeTileEntityRendererFast;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BeltTunnelTileEntityRenderer extends SafeTileEntityRendererFast<BeltTunnelTileEntity> {

	@Override
	public void renderFast(BeltTunnelTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		SuperByteBuffer flapBuffer = AllBlockPartials.BELT_TUNNEL_FLAP.renderOn(te.getBlockState());
		SuperByteBuffer indicatorBuffer = AllBlockPartials.BELT_TUNNEL_INDICATOR.renderOn(te.getBlockState());
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
				float abs = Math.abs(f);
				float flapAngle = MathHelper.sin((float) ((1 - abs) * Math.PI * intensity)) * 30 * -f;
				flapAngle = (float) (flapAngle / 180 * Math.PI);

				if (te.syncedFlaps.containsKey(direction)) {
					float lightIntensity = abs * abs * abs;
					int color = ColorHelper.mixColors(0x808080, 0xFFFFFF, lightIntensity);
					indicatorBuffer.rotateCentered(Axis.Y, (float) ((horizontalAngle + 90) / 180f * Math.PI))
							.translate(x, y, z).color(color)
							.light(world.getCombinedLight(pos, (int) (12 * lightIntensity))).renderInto(buffer);
				}

				flapBuffer.translate(0, 0, -segment * 3 / 16f);
				flapBuffer.translate(flapPivotX, flapPivotY, flapPivotZ).rotate(Axis.Z, flapAngle)
						.translate(-flapPivotX, -flapPivotY, -flapPivotZ);
				flapBuffer.rotateCentered(Axis.Y, (float) (horizontalAngle / 180f * Math.PI)).translate(x, y, z);
				flapBuffer.light(te.getBlockState().getPackedLightmapCoords(world, pos)).renderInto(buffer);
			}
		}

	}

}
