package com.simibubi.create.modules.logistics.block.belts;

import com.mojang.blaze3d.platform.GLX;
import com.simibubi.create.foundation.behaviour.filtering.FilteringRenderer;

import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.state.properties.BlockStateProperties;

public class BeltObserverTileEntityRenderer extends TileEntityRenderer<BeltObserverTileEntity> {

	@Override
	public void render(BeltObserverTileEntity tileEntityIn, double x, double y, double z, float partialTicks,
			int destroyStage) {
		super.render(tileEntityIn, x, y, z, partialTicks, destroyStage);

		int i = tileEntityIn.getWorld().getCombinedLight(tileEntityIn.getPos().up()
				.offset(tileEntityIn.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING)), 0);
		int j = i % 65536;
		int k = i / 65536;
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float) j, (float) k);

		FilteringRenderer.renderOnTileEntity(tileEntityIn, x, y, z, partialTicks, destroyStage);
	}

}
