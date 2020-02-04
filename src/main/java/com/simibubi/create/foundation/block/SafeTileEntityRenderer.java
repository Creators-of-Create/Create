package com.simibubi.create.foundation.block;

import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.tileentity.TileEntity;

public abstract class SafeTileEntityRenderer<T extends TileEntity> extends TileEntityRenderer<T> {

	@Override
	public final void render(T te, double x, double y, double z, float partialTicks, int destroyStage) {
		if (isInvalid(te))
			return;
		renderWithGL(te, x, y, z, partialTicks, destroyStage);
	}
	
	protected abstract void renderWithGL(T tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage);
	
	@Override
	public final void renderTileEntityFast(T te, double x, double y, double z, float partialTicks, int destroyStage,
			BufferBuilder buffer) {
		if (isInvalid(te))
			return;
		renderFast(te, x, y, z, partialTicks, destroyStage, buffer);
	}
	
	protected void renderFast(T tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage, BufferBuilder buffer) {
	}
	
	public boolean isInvalid(T te) {
		return te.getBlockState().getBlock() == Blocks.AIR;
	}
}
