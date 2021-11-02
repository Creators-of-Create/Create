package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
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
 * Taken from EntityRendererManager
 */
public class ShadowRenderHelper {

	private static final RenderType SHADOW_LAYER =
		RenderType.entityNoOutline(new ResourceLocation("textures/misc/shadow.png"));

	public static void renderShadow(MatrixStack matrixStack, IRenderTypeBuffer buffer, float opacity, float radius) {
		MatrixStack.Entry entry = matrixStack.last();
		IVertexBuilder builder = buffer.getBuffer(SHADOW_LAYER);

		opacity /= 2;
		shadowVertex(entry, builder, opacity, -1 * radius, 0, -1 * radius, 0, 0);
		shadowVertex(entry, builder, opacity, -1 * radius, 0, 1 * radius, 0, 1);
		shadowVertex(entry, builder, opacity, 1 * radius, 0, 1 * radius, 1, 1);
		shadowVertex(entry, builder, opacity, 1 * radius, 0, -1 * radius, 1, 0);
	}

	public static void renderShadow(MatrixStack matrixStack, IRenderTypeBuffer buffer, IWorldReader world,
		Vector3d pos, float opacity, float radius) {
		float f = radius;

		double d2 = pos.x();
		double d0 = pos.y();
		double d1 = pos.z();
		int i = MathHelper.floor(d2 - (double) f);
		int j = MathHelper.floor(d2 + (double) f);
		int k = MathHelper.floor(d0 - (double) f);
		int l = MathHelper.floor(d0);
		int i1 = MathHelper.floor(d1 - (double) f);
		int j1 = MathHelper.floor(d1 + (double) f);
		MatrixStack.Entry entry = matrixStack.last();
		IVertexBuilder builder = buffer.getBuffer(SHADOW_LAYER);

		for (BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(i, k, i1), new BlockPos(j, l, j1))) {
			renderBlockShadow(entry, builder, world, blockpos, d2, d0, d1, f,
				opacity);
		}
	}

	private static void renderBlockShadow(MatrixStack.Entry entry, IVertexBuilder builder,
		IWorldReader world, BlockPos pos, double x, double y, double z,
		float radius, float opacity) {
		BlockPos blockpos = pos.below();
		BlockState blockstate = world.getBlockState(blockpos);
		if (blockstate.getRenderShape() != BlockRenderType.INVISIBLE && world.getMaxLocalRawBrightness(pos) > 3) {
			if (blockstate.isCollisionShapeFullBlock(world, blockpos)) {
				VoxelShape voxelshape = blockstate.getShape(world, pos.below());
				if (!voxelshape.isEmpty()) {
					@SuppressWarnings("deprecation")
					float f = (float) (((double) opacity - (y - (double) pos.getY()) / 2.0D)
						* 0.5D * (double) world.getBrightness(pos));
					if (f >= 0.0F) {
						if (f > 1.0F) {
							f = 1.0F;
						}

						AxisAlignedBB axisalignedbb = voxelshape.bounds();
						double d0 = (double) pos.getX() + axisalignedbb.minX;
						double d1 = (double) pos.getX() + axisalignedbb.maxX;
						double d2 = (double) pos.getY() + axisalignedbb.minY;
						double d3 = (double) pos.getZ() + axisalignedbb.minZ;
						double d4 = (double) pos.getZ() + axisalignedbb.maxZ;
						float f1 = (float) (d0 - x);
						float f2 = (float) (d1 - x);
						float f3 = (float) (d2 - y + 0.015625D);
						float f4 = (float) (d3 - z);
						float f5 = (float) (d4 - z);
						float f6 = -f1 / 2.0F / radius + 0.5F;
						float f7 = -f2 / 2.0F / radius + 0.5F;
						float f8 = -f4 / 2.0F / radius + 0.5F;
						float f9 = -f5 / 2.0F / radius + 0.5F;
						shadowVertex(entry, builder, f, f1, f3, f4, f6, f8);
						shadowVertex(entry, builder, f, f1, f3, f5, f6, f9);
						shadowVertex(entry, builder, f, f2, f3, f5, f7, f9);
						shadowVertex(entry, builder, f, f2, f3, f4, f7, f8);
					}
				}
			}
		}
	}

	private static void shadowVertex(MatrixStack.Entry entry, IVertexBuilder builder, float alpha,
		float x, float y, float z, float u, float v) {
		builder.vertex(entry.pose(), x, y, z)
			.color(1.0F, 1.0F, 1.0F, alpha)
			.uv(u, v)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(0xF000F0)
			.normal(entry.normal(), 0.0F, 1.0F, 0.0F)
			.endVertex();
	}

}
