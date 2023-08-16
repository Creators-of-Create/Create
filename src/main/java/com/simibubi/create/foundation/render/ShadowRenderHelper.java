package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Taken from EntityRendererManager
 */
public class ShadowRenderHelper {

	private static final RenderType SHADOW_LAYER =
		RenderType.entityNoOutline(new ResourceLocation("textures/misc/shadow.png"));

	public static void renderShadow(PoseStack matrixStack, MultiBufferSource buffer, float opacity, float radius) {
		PoseStack.Pose entry = matrixStack.last();
		VertexConsumer builder = buffer.getBuffer(SHADOW_LAYER);

		opacity /= 2;
		shadowVertex(entry, builder, opacity, -1 * radius, 0, -1 * radius, 0, 0);
		shadowVertex(entry, builder, opacity, -1 * radius, 0, 1 * radius, 0, 1);
		shadowVertex(entry, builder, opacity, 1 * radius, 0, 1 * radius, 1, 1);
		shadowVertex(entry, builder, opacity, 1 * radius, 0, -1 * radius, 1, 0);
	}

	public static void renderShadow(PoseStack matrixStack, MultiBufferSource buffer, LevelReader world,
		Vec3 pos, float opacity, float radius) {
		float f = radius;

		double d2 = pos.x();
		double d0 = pos.y();
		double d1 = pos.z();
		int i = Mth.floor(d2 - (double) f);
		int j = Mth.floor(d2 + (double) f);
		int k = Mth.floor(d0 - (double) f);
		int l = Mth.floor(d0);
		int i1 = Mth.floor(d1 - (double) f);
		int j1 = Mth.floor(d1 + (double) f);
		PoseStack.Pose entry = matrixStack.last();
		VertexConsumer builder = buffer.getBuffer(SHADOW_LAYER);

		for (BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(i, k, i1), new BlockPos(j, l, j1))) {
			renderBlockShadow(entry, builder, world, blockpos, d2, d0, d1, f,
				opacity);
		}
	}

	private static void renderBlockShadow(PoseStack.Pose entry, VertexConsumer builder,
		LevelReader world, BlockPos pos, double x, double y, double z,
		float radius, float opacity) {
		BlockPos blockpos = pos.below();
		BlockState blockstate = world.getBlockState(blockpos);
		if (blockstate.getRenderShape() != RenderShape.INVISIBLE && world.getMaxLocalRawBrightness(pos) > 3) {
			if (blockstate.isCollisionShapeFullBlock(world, blockpos)) {
				VoxelShape voxelshape = blockstate.getShape(world, pos.below());
				if (!voxelshape.isEmpty()) {
					float brightness = LightTexture.getBrightness(world.dimensionType(), world.getMaxLocalRawBrightness(pos));
					float f = (float) ((opacity - (y - pos.getY()) / 2.0D) * 0.5D * brightness);
					if (f >= 0.0F) {
						if (f > 1.0F) {
							f = 1.0F;
						}

						AABB AABB = voxelshape.bounds();
						double d0 = (double) pos.getX() + AABB.minX;
						double d1 = (double) pos.getX() + AABB.maxX;
						double d2 = (double) pos.getY() + AABB.minY;
						double d3 = (double) pos.getZ() + AABB.minZ;
						double d4 = (double) pos.getZ() + AABB.maxZ;
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

	private static void shadowVertex(PoseStack.Pose entry, VertexConsumer builder, float alpha,
		float x, float y, float z, float u, float v) {
		builder.vertex(entry.pose(), x, y, z)
			.color(1.0F, 1.0F, 1.0F, alpha)
			.uv(u, v)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(LightTexture.FULL_BRIGHT)
			.normal(entry.normal(), 0.0F, 1.0F, 0.0F)
			.endVertex();
	}

}
