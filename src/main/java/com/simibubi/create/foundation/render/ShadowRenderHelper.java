package com.simibubi.create.foundation.render;

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
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;

/**
 * Stolen from EntityRendererManager
 */
public class ShadowRenderHelper {

	private static final RenderType SHADOW_LAYER =
		RenderType.entityNoOutline(new ResourceLocation("textures/misc/shadow.png"));

	public static void renderShadow(MatrixStack pMatrixStackIn, IRenderTypeBuffer pBufferIn, Vector3d pos,
		float pWeightIn, float pSizeIn) {
		float f = pSizeIn;

		double d2 = pos.x();
		double d0 = pos.y();
		double d1 = pos.z();
		int i = MathHelper.floor(d2 - (double) f);
		int j = MathHelper.floor(d2 + (double) f);
		int k = MathHelper.floor(d0 - (double) f);
		int l = MathHelper.floor(d0);
		int i1 = MathHelper.floor(d1 - (double) f);
		int j1 = MathHelper.floor(d1 + (double) f);
		MatrixStack.Entry matrixstack$entry = pMatrixStackIn.last();
		IVertexBuilder ivertexbuilder = pBufferIn.getBuffer(SHADOW_LAYER);

		for (BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(i, k, i1), new BlockPos(j, l, j1))) {
			renderShadowPart(matrixstack$entry, ivertexbuilder, Minecraft.getInstance().level, blockpos, d2, d0, d1, f,
				pWeightIn);
		}

	}

	private static void renderShadowPart(MatrixStack.Entry pMatrixEntryIn, IVertexBuilder pBufferIn,
		IWorldReader pWorldIn, BlockPos pBlockPosIn, double pXIn, double pYIn, double pZIn,
		float pSizeIn, float pWeightIn) {
		BlockPos blockpos = pBlockPosIn.below();
		BlockState blockstate = pWorldIn.getBlockState(blockpos);
		if (blockstate.getRenderShape() != BlockRenderType.INVISIBLE && pWorldIn.getMaxLocalRawBrightness(pBlockPosIn) > 3) {
			if (blockstate.isCollisionShapeFullBlock(pWorldIn, blockpos)) {
				VoxelShape voxelshape = blockstate.getShape(pWorldIn, pBlockPosIn.below());
				if (!voxelshape.isEmpty()) {
					@SuppressWarnings("deprecation")
					float f = (float) (((double) pWeightIn - (pYIn - (double) pBlockPosIn.getY()) / 2.0D)
						* 0.5D * (double) pWorldIn.getBrightness(pBlockPosIn));
					if (f >= 0.0F) {
						if (f > 1.0F) {
							f = 1.0F;
						}

						AxisAlignedBB axisalignedbb = voxelshape.bounds();
						double d0 = (double) pBlockPosIn.getX() + axisalignedbb.minX;
						double d1 = (double) pBlockPosIn.getX() + axisalignedbb.maxX;
						double d2 = (double) pBlockPosIn.getY() + axisalignedbb.minY;
						double d3 = (double) pBlockPosIn.getZ() + axisalignedbb.minZ;
						double d4 = (double) pBlockPosIn.getZ() + axisalignedbb.maxZ;
						float f1 = (float) (d0 - pXIn);
						float f2 = (float) (d1 - pXIn);
						float f3 = (float) (d2 - pYIn + 0.015625D);
						float f4 = (float) (d3 - pZIn);
						float f5 = (float) (d4 - pZIn);
						float f6 = -f1 / 2.0F / pSizeIn + 0.5F;
						float f7 = -f2 / 2.0F / pSizeIn + 0.5F;
						float f8 = -f4 / 2.0F / pSizeIn + 0.5F;
						float f9 = -f5 / 2.0F / pSizeIn + 0.5F;
						shadowVertex(pMatrixEntryIn, pBufferIn, f, f1, f3, f4, f6, f8);
						shadowVertex(pMatrixEntryIn, pBufferIn, f, f1, f3, f5, f6, f9);
						shadowVertex(pMatrixEntryIn, pBufferIn, f, f2, f3, f5, f7, f9);
						shadowVertex(pMatrixEntryIn, pBufferIn, f, f2, f3, f4, f7, f8);
					}

				}
			}
		}
	}

	private static void shadowVertex(MatrixStack.Entry pMatrixEntryIn, IVertexBuilder pBufferIn, float pAlphaIn,
		float pXIn, float pYIn, float pZIn, float pTexU, float pTexV) {
		pBufferIn.vertex(pMatrixEntryIn.pose(), pXIn, pYIn, pZIn)
			.color(1.0F, 1.0F, 1.0F, pAlphaIn)
			.uv(pTexU, pTexV)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(15728880)
			.normal(pMatrixEntryIn.normal(), 0.0F, 1.0F, 0.0F)
			.endVertex();
	}
}
