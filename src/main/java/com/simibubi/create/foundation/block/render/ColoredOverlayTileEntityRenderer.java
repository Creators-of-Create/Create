package com.simibubi.create.foundation.block.render;

import com.simibubi.create.foundation.block.SafeTileEntityRendererFast;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ColoredOverlayTileEntityRenderer<T extends TileEntity> extends SafeTileEntityRendererFast<T> {

	@Override
	public void renderFast(T te, double x, double y, double z, float partialTicks, int destroyStage,
			BufferBuilder buffer) {
		SuperByteBuffer render = render(getWorld(), te.getPos(), te.getBlockState(), getOverlayBuffer(te),
				getColor(te, partialTicks));
		buffer.putBulkData(render.translate(x, y, z).build());
	}

	protected abstract int getColor(T te, float partialTicks);

	protected abstract SuperByteBuffer getOverlayBuffer(T te);

	public static SuperByteBuffer render(World world, BlockPos pos, BlockState state, SuperByteBuffer buffer,
			int color) {
		int packedLightmapCoords = state.getPackedLightmapCoords(world, pos);
		return buffer.color(color).light(packedLightmapCoords);
	}

}
