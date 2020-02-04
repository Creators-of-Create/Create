package com.simibubi.create.foundation.block;

import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.model.animation.TileEntityRendererFast;

public abstract class SafeTileEntityRendererFast<T extends TileEntity> extends TileEntityRendererFast<T> {

	@Override
	public final void renderTileEntityFast(T te, double x, double y, double z, float partialTicks, int destroyStage,
			BufferBuilder buffer) {
		if (isInvalid(te))
			return;
		renderFast(te, x, y, z, partialTicks, destroyStage, buffer);
	}

	protected abstract void renderFast(T te, double x, double y, double z, float partialTicks, int destroyStage,
			BufferBuilder buffer);

	public boolean isInvalid(T te) {
		return te.getBlockState().getBlock() == Blocks.AIR;
	}

}
