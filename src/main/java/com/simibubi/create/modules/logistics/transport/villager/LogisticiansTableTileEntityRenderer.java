package com.simibubi.create.modules.logistics.transport.villager;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.foundation.utility.ColoredIndicatorRenderer;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class LogisticiansTableTileEntityRenderer extends TileEntityRenderer<LogisticiansTableTileEntity> {
	private static final ResourceLocation bookLocation = new ResourceLocation(
			"textures/entity/enchanting_table_book.png");
	private final BookModel bookModel = new BookModel();

	public void render(LogisticiansTableTileEntity tileEntityIn, double x, double y, double z, float partialTicks,
			int destroyStage) {
		Minecraft.getInstance().textureManager
				.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();

		if (net.minecraft.client.Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
		else
			GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.color3f(1, 1, 1);
		BlockPos pos = tileEntityIn.getPos();
		BlockState blockState = tileEntityIn.getBlockState();
		BlockState renderedState = AllBlocks.LOGISTICIANS_TABLE_INDICATOR.get().getDefaultState();
		int packedLightmapCoords = blockState.getPackedLightmapCoords(getWorld(), pos);
		Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		int color = tileEntityIn.getColor();
		Tessellator.getInstance().getBuffer().putBulkData(ColoredIndicatorRenderer.get(renderedState)
				.getTransformed((float) x, (float) y, (float) z, color, packedLightmapCoords));
		Tessellator.getInstance().draw();
		RenderHelper.enableStandardItemLighting();

		BlockState blockstate = tileEntityIn.getBlockState();
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
