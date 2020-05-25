package com.simibubi.create.content.logistics.block.belts.tunnel;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BeltTunnelRenderer extends SafeTileEntityRenderer<BeltTunnelTileEntity> {

	public BeltTunnelRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(BeltTunnelTileEntity te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
		int light, int overlay) {
		SuperByteBuffer flapBuffer = AllBlockPartials.BELT_TUNNEL_FLAP.renderOn(te.getBlockState());
		SuperByteBuffer indicatorBuffer = AllBlockPartials.BELT_TUNNEL_INDICATOR.renderOn(te.getBlockState());
		BlockPos pos = te.getPos();
		World world = te.getWorld();

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

				float f = te.flaps.get(direction)
					.get(partialTicks);
				if (direction.getAxis() == Axis.X)
					f *= -1;

				float intensity = segment == 3 ? 1.5f : segment + 1;
				float abs = Math.abs(f);
				float flapAngle = MathHelper.sin((float) ((1 - abs) * Math.PI * intensity)) * 30 * -f;
				flapAngle = (float) (flapAngle / 180 * Math.PI);

				IVertexBuilder vb = buffer.getBuffer(RenderType.getSolid());

				if (te.syncedFlaps.containsKey(direction)) {
					float lightIntensity = abs * abs * abs;
					int indicatorLight = WorldRenderer.getLightmapCoordinates(world, pos);
					int indicatorBlockLight = LightTexture.getBlockLightCoordinates(indicatorLight);
					int indicatorSkyLight = LightTexture.getSkyLightCoordinates(indicatorLight);
					indicatorBlockLight = Math.max(indicatorBlockLight, (int) (12 * lightIntensity));
					indicatorLight = LightTexture.pack(indicatorBlockLight, indicatorSkyLight);
					int color = ColorHelper.mixColors(0x808080, 0xFFFFFF, lightIntensity);
					indicatorBuffer.rotateCentered(Direction.UP, (float) ((horizontalAngle + 90) / 180f * Math.PI))
						.color(color)
						.light(indicatorLight)
						.renderInto(ms, vb);
				}

				flapBuffer.rotateCentered(Direction.UP, (float) (horizontalAngle / 180f * Math.PI));
				flapBuffer.translate(-flapPivotX, -flapPivotY, -flapPivotZ)
					.rotate(Direction.SOUTH, flapAngle)
					.translate(flapPivotX, flapPivotY, flapPivotZ);
				flapBuffer.translate(0, 0, -segment * 3 / 16f);
				flapBuffer.light(WorldRenderer.getLightmapCoordinates(world, te.getBlockState(), pos))
					.renderInto(ms, vb);
			}
		}

	}

}
