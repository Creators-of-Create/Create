package com.simibubi.create.modules.logistics.transport.villager;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.block.render.ColoredOverlayTileEntityRenderer;
import com.simibubi.create.foundation.utility.SuperByteBuffer;
import com.simibubi.create.foundation.utility.TessellatorHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ResourceLocation;

public class LogisticiansTableTileEntityRenderer extends TileEntityRenderer<LogisticiansTableTileEntity> {

	public void render(LogisticiansTableTileEntity te, double x, double y, double z, float partialTicks,
			int destroyStage) {
		renderColoredIndicator(te, x, y, z);
		renderBook(te, x, y, z);
	}

	protected void renderColoredIndicator(LogisticiansTableTileEntity te, double x, double y, double z) {
		TessellatorHelper.prepareFastRender();
		BlockState renderedState = AllBlocks.LOGISTICIANS_TABLE_INDICATOR.get().getDefaultState();
		TessellatorHelper.begin(DefaultVertexFormats.BLOCK);
		SuperByteBuffer render = ColoredOverlayTileEntityRenderer.render(getWorld(), te.getPos(), renderedState,
				te.getColor());
		Tessellator.getInstance().getBuffer().putBulkData(render.translate(x, y, z).build());
		TessellatorHelper.draw();
	}

	private static final ResourceLocation bookLocation = new ResourceLocation(
			"textures/entity/enchanting_table_book.png");
	private final BookModel bookModel = new BookModel();

	protected void renderBook(LogisticiansTableTileEntity te, double x, double y, double z) {
		RenderHelper.enableStandardItemLighting();
		BlockState blockstate = te.getBlockState();
		GlStateManager.pushMatrix();
		GlStateManager.translatef((float) x + 0.5F, (float) y + 1.0F + 0.0625F, (float) z + 0.5F);
		float f = blockstate.get(BlockStateProperties.HORIZONTAL_FACING).rotateY().getHorizontalAngle();
		GlStateManager.rotatef(-f, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotatef(67.5F, 0.0F, 0.0F, 1.0F);
		GlStateManager.translatef(0.0F, -0.125F, 0.0F);
		this.bindTexture(bookLocation);
		GlStateManager.enableCull();
		this.bookModel.render(0.0F, 0.1F, 0.9F, 1.2F, 0.0F, 0.0625F);
		GlStateManager.disableCull();
		GlStateManager.popMatrix();
	}
}
