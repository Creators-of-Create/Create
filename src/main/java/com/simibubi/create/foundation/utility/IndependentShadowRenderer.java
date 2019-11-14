package com.simibubi.create.foundation.utility;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;

/**
 * Stolen from EntityRenderer
 */
public class IndependentShadowRenderer {

	private static final ResourceLocation SHADOW_TEXTURES = new ResourceLocation("textures/misc/shadow.png");

	public static void renderShadow(double x, double y, double z, float shadowAlpha, float size) {
		GlStateManager.enableBlend();
		GlStateManager.enableAlphaTest();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.DST_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		Minecraft.getInstance().getTextureManager().bindTexture(SHADOW_TEXTURES);
		IWorldReader iworldreader = Minecraft.getInstance().world;
		GlStateManager.depthMask(false);
		int i = MathHelper.floor(x - size);
		int j = MathHelper.floor(x + size);
		int k = MathHelper.floor(y - size);
		int l = MathHelper.floor(y);
		int i1 = MathHelper.floor(z - size);
		int j1 = MathHelper.floor(z + size);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

		for (BlockPos blockpos : BlockPos.getAllInBoxMutable(new BlockPos(i, k, i1), new BlockPos(j, l, j1))) {
			BlockPos blockpos1 = blockpos.down();
			BlockState blockstate = iworldreader.getBlockState(blockpos1);
			if (blockstate.getRenderType() != BlockRenderType.INVISIBLE && iworldreader.getLight(blockpos) > 3) {
				func_217759_a(blockstate, iworldreader, blockpos1, 0, 0, 0, blockpos, shadowAlpha, size, -x, -y, -z);
			}
		}

		tessellator.draw();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
	}

	private static void func_217759_a(BlockState p_217759_1_, IWorldReader p_217759_2_, BlockPos p_217759_3_,
			double p_217759_4_, double p_217759_6_, double p_217759_8_, BlockPos p_217759_10_, float p_217759_11_,
			float p_217759_12_, double p_217759_13_, double p_217759_15_, double p_217759_17_) {
		ClientWorld world = Minecraft.getInstance().world;
		VoxelShape voxelshape = p_217759_1_.getShape(world, p_217759_10_.down());
		if (!voxelshape.isEmpty()) {
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			double d0 = ((double) p_217759_11_ - (p_217759_6_ - ((double) p_217759_10_.getY() + p_217759_15_)) / 2.0D)
					* 0.5D * (double) world.getBrightness(p_217759_10_);
			if (!(d0 < 0.0D)) {
				if (d0 > 1.0D) {
					d0 = 1.0D;
				}

				AxisAlignedBB axisalignedbb = voxelshape.getBoundingBox();
				double d1 = (double) p_217759_10_.getX() + axisalignedbb.minX + p_217759_13_;
				double d2 = (double) p_217759_10_.getX() + axisalignedbb.maxX + p_217759_13_;
				double d3 = (double) p_217759_10_.getY() + axisalignedbb.minY + p_217759_15_ + 0.015625D;
				double d4 = (double) p_217759_10_.getZ() + axisalignedbb.minZ + p_217759_17_;
				double d5 = (double) p_217759_10_.getZ() + axisalignedbb.maxZ + p_217759_17_;
				float f = (float) ((p_217759_4_ - d1) / 2.0D / (double) p_217759_12_ + 0.5D);
				float f1 = (float) ((p_217759_4_ - d2) / 2.0D / (double) p_217759_12_ + 0.5D);
				float f2 = (float) ((p_217759_8_ - d4) / 2.0D / (double) p_217759_12_ + 0.5D);
				float f3 = (float) ((p_217759_8_ - d5) / 2.0D / (double) p_217759_12_ + 0.5D);
				bufferbuilder.pos(d1, d3, d4).tex((double) f, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0)
						.endVertex();
				bufferbuilder.pos(d1, d3, d5).tex((double) f, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0)
						.endVertex();
				bufferbuilder.pos(d2, d3, d5).tex((double) f1, (double) f3).color(1.0F, 1.0F, 1.0F, (float) d0)
						.endVertex();
				bufferbuilder.pos(d2, d3, d4).tex((double) f1, (double) f2).color(1.0F, 1.0F, 1.0F, (float) d0)
						.endVertex();
			}
		}
	}

}
