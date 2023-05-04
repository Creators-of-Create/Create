package com.simibubi.create.content.logistics.trains;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.trains.track.AbstractBogeyTileEntity;
import com.simibubi.create.foundation.tileEntity.renderer.SafeTileEntityRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BogeyTileEntityRenderer<T extends BlockEntity> extends SafeTileEntityRenderer<T> {

	public BogeyTileEntityRenderer(BlockEntityRendererProvider.Context context) {}

	@Override
	protected void renderSafe(T te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light,
		int overlay) {
		BlockState blockState = te.getBlockState();
		if (te instanceof AbstractBogeyTileEntity sbte) {
			float angle = sbte.getVirtualAngle(partialTicks);
			if (blockState.getBlock() instanceof AbstractBogeyBlock bogey)
				bogey.render(blockState, bogey.isUpsideDown(blockState), angle, ms, partialTicks, buffer, light, overlay, sbte);
		}
	}

}
