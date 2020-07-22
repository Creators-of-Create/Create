package com.simibubi.create.foundation.utility;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;

/**
 * Stolen from EntityRendererManager
 */
public class ShadowRenderHelper {

	private static final RenderType SHADOW_LAYER = RenderType.getEntityNoOutline(new ResourceLocation("textures/misc/shadow.png"));

	public static void renderShadow(MatrixStack p_229096_0_, IRenderTypeBuffer p_229096_1_, Vector3d pos,
			float p_229096_3_, float p_229096_6_) {
		float f = p_229096_6_;

		double d2 = pos.getX();
		double d0 = pos.getY();
		double d1 = pos.getZ();
		int i = MathHelper.floor(d2 - (double) f);
		int j = MathHelper.floor(d2 + (double) f);
		int k = MathHelper.floor(d0 - (double) f);
		int l = MathHelper.floor(d0);
		int i1 = MathHelper.floor(d1 - (double) f);
		int j1 = MathHelper.floor(d1 + (double) f);
		MatrixStack.Entry matrixstack$entry = p_229096_0_.peek();
		IVertexBuilder ivertexbuilder = p_229096_1_.getBuffer(SHADOW_LAYER);

		for (BlockPos blockpos : BlockPos.getAllInBoxMutable(new BlockPos(i, k, i1), new BlockPos(j, l, j1))) {
			renderShadowPart(matrixstack$entry, ivertexbuilder, Minecraft.getInstance().world, blockpos, d2, d0, d1, f, p_229096_3_);
		}

	}

	private static void renderShadowPart(MatrixStack.Entry p_229092_0_, IVertexBuilder p_229092_1_,
			IWorldReader p_229092_2_, BlockPos p_229092_3_, double p_229092_4_, double p_229092_6_, double p_229092_8_,
			float p_229092_10_, float p_229092_11_) {
		BlockPos blockpos = p_229092_3_.down();
		BlockState blockstate = p_229092_2_.getBlockState(blockpos);
		if (blockstate.getRenderType() != BlockRenderType.INVISIBLE && p_229092_2_.getLight(p_229092_3_) > 3) {
			if (blockstate.isFullCube(p_229092_2_, blockpos)) {
				VoxelShape voxelshape = blockstate.getShape(p_229092_2_, p_229092_3_.down());
				if (!voxelshape.isEmpty()) {
					@SuppressWarnings("deprecation")
					float f = (float) (((double) p_229092_11_ - (p_229092_6_ - (double) p_229092_3_.getY()) / 2.0D)
							* 0.5D * (double) p_229092_2_.getBrightness(p_229092_3_));
					if (f >= 0.0F) {
						if (f > 1.0F) {
							f = 1.0F;
						}

						AxisAlignedBB axisalignedbb = voxelshape.getBoundingBox();
						double d0 = (double) p_229092_3_.getX() + axisalignedbb.minX;
						double d1 = (double) p_229092_3_.getX() + axisalignedbb.maxX;
						double d2 = (double) p_229092_3_.getY() + axisalignedbb.minY;
						double d3 = (double) p_229092_3_.getZ() + axisalignedbb.minZ;
						double d4 = (double) p_229092_3_.getZ() + axisalignedbb.maxZ;
						float f1 = (float) (d0 - p_229092_4_);
						float f2 = (float) (d1 - p_229092_4_);
						float f3 = (float) (d2 - p_229092_6_ + 0.015625D);
						float f4 = (float) (d3 - p_229092_8_);
						float f5 = (float) (d4 - p_229092_8_);
						float f6 = -f1 / 2.0F / p_229092_10_ + 0.5F;
						float f7 = -f2 / 2.0F / p_229092_10_ + 0.5F;
						float f8 = -f4 / 2.0F / p_229092_10_ + 0.5F;
						float f9 = -f5 / 2.0F / p_229092_10_ + 0.5F;
						shadowVertex(p_229092_0_, p_229092_1_, f, f1, f3, f4, f6, f8);
						shadowVertex(p_229092_0_, p_229092_1_, f, f1, f3, f5, f6, f9);
						shadowVertex(p_229092_0_, p_229092_1_, f, f2, f3, f5, f7, f9);
						shadowVertex(p_229092_0_, p_229092_1_, f, f2, f3, f4, f7, f8);
					}

				}
			}
		}
	}

	private static void shadowVertex(MatrixStack.Entry p_229091_0_, IVertexBuilder p_229091_1_, float p_229091_2_,
			float p_229091_3_, float p_229091_4_, float p_229091_5_, float p_229091_6_, float p_229091_7_) {
		p_229091_1_.vertex(p_229091_0_.getModel(), p_229091_3_, p_229091_4_, p_229091_5_)
				.color(1.0F, 1.0F, 1.0F, p_229091_2_).texture(p_229091_6_, p_229091_7_)
				.overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(p_229091_0_.getNormal(), 0.0F, 1.0F, 0.0F)
				.endVertex();
	}
}
