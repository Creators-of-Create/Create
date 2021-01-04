package com.simibubi.create.foundation.tileEntity.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.render.SuperByteBuffer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ColoredOverlayTileEntityRenderer<T extends TileEntity> extends SafeTileEntityRenderer<T> {

	public ColoredOverlayTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	protected void renderSafe(T te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer,
			int light, int overlay) {
		SuperByteBuffer render = render(te.getWorld(), te.getPos(), te.getBlockState(), getOverlayBuffer(te),
				getColor(te, partialTicks));
		render.renderInto(ms, buffer.getBuffer(RenderType.getSolid()));
	}

	protected abstract int getColor(T te, float partialTicks);

	protected abstract SuperByteBuffer getOverlayBuffer(T te);

	public static SuperByteBuffer render(World world, BlockPos pos, BlockState state, SuperByteBuffer buffer,
			int color) {
		int packedLightmapCoords = WorldRenderer.getLightmapCoordinates(world, state, pos);
		return buffer.color(color).light(packedLightmapCoords);
	}

}
