package com.simibubi.create.foundation.block.render;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.utility.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.animation.TileEntityRendererFast;

public abstract class ColoredOverlayTileEntityRenderer<T extends TileEntity> extends TileEntityRendererFast<T> {

	@Override
	public void renderTileEntityFast(T te, double x, double y, double z, float partialTicks, int destroyStage,
			BufferBuilder buffer) {
		SuperByteBuffer render = render(getWorld(), te.getPos(), getOverlayState(te), getColor(te, partialTicks));
		buffer.putBulkData(render.translate(x, y, z).build());
	}

	protected abstract int getColor(T te, float partialTicks);

	protected abstract BlockState getOverlayState(T te);

	public static SuperByteBuffer render(World world, BlockPos pos, BlockState state, int color) {
		int packedLightmapCoords = state.getPackedLightmapCoords(world, pos);
		SuperByteBuffer buffer = CreateClient.bufferCache.renderGenericBlockModel(state);
		return buffer.color(color).light(packedLightmapCoords);
	}

}
