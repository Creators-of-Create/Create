package com.simibubi.create.modules.contraptions.receivers.crafter;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.TessellatorHelper;
import com.simibubi.create.modules.contraptions.base.KineticTileEntityRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.state.properties.BlockStateProperties;

public class MechanicalCrafterTileEntityRenderer extends TileEntityRenderer<MechanicalCrafterTileEntity> {

	@Override
	public void render(MechanicalCrafterTileEntity tileEntityIn, double x, double y, double z, float partialTicks,
			int destroyStage) {
		super.render(tileEntityIn, x, y, z, partialTicks, destroyStage);

		TessellatorHelper.prepareFastRender();
		TessellatorHelper.begin(DefaultVertexFormats.BLOCK);
		renderTileEntityFast(tileEntityIn, x, y, z, partialTicks, destroyStage, Tessellator.getInstance().getBuffer());
		TessellatorHelper.draw();
	}

	@Override
	public void renderTileEntityFast(MechanicalCrafterTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage, BufferBuilder buffer) {
		BlockState renderedState = AllBlocks.SHAFTLESS_COGWHEEL.get().getDefaultState().with(BlockStateProperties.AXIS,
				te.getBlockState().get(BlockStateProperties.FACING).getAxis());
		KineticTileEntityRenderer.renderRotatingKineticBlock(te, getWorld(), renderedState, x, y, z, buffer);
	}

}
