package com.simibubi.create.foundation.tileEntity.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;

public abstract class SafeTileEntityRenderer<T extends TileEntity> extends TileEntityRenderer<T> {

	public SafeTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
		super(dispatcher);
	}

	@Override
	public final void render(T te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light,
		int overlay) {
		if (isInvalid(te))
			return;
		renderSafe(te, partialTicks, ms, buffer, light, overlay);
	}

	protected abstract void renderSafe(T te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffer, int light,
		int overlay);

	public boolean isInvalid(T te) {
		return !te.hasLevel() || te.getBlockState()
			.getBlock() == Blocks.AIR;
	}
}
