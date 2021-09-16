package com.simibubi.create.foundation.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelReader;

/**
 * Stolen from EntityRendererManager
 */
public class ShadowRenderHelper {

	private static final RenderType SHADOW_LAYER =
		RenderType.entityNoOutline(new ResourceLocation("textures/misc/shadow.png"));

	public static void renderShadow(PoseStack p_229096_0_, MultiBufferSource p_229096_1_, Vec3 pos,
		float p_229096_3_, float p_229096_6_) {
		float f = p_229096_6_;

		double d2 = pos.x();
		double d0 = pos.y();
		double d1 = pos.z();
		int i = Mth.floor(d2 - (double) f);
		int j = Mth.floor(d2 + (double) f);
		int k = Mth.floor(d0 - (double) f);
		int l = Mth.floor(d0);
		int i1 = Mth.floor(d1 - (double) f);
		int j1 = Mth.floor(d1 + (double) f);
		PoseStack.Pose matrixstack$entry = p_229096_0_.last();
		VertexConsumer ivertexbuilder = p_229096_1_.getBuffer(SHADOW_LAYER);

		for (BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(i, k, i1), new BlockPos(j, l, j1))) {
			renderShadowPart(matrixstack$entry, ivertexbuilder, Minecraft.getInstance().level, blockpos, d2, d0, d1, f,
				p_229096_3_);
		}

	}

	private static void renderShadowPart(PoseStack.Pose p_229092_0_, VertexConsumer p_229092_1_,
		LevelReader p_229092_2_, BlockPos p_229092_3_, double p_229092_4_, double p_229092_6_, double p_229092_8_,
		float p_229092_10_, float p_229092_11_) {
		BlockPos blockpos = p_229092_3_.below();
		BlockState blockstate = p_229092_2_.getBlockState(blockpos);
		if (blockstate.getRenderShape() != RenderShape.INVISIBLE && p_229092_2_.getMaxLocalRawBrightness(p_229092_3_) > 3) {
			if (blockstate.isCollisionShapeFullBlock(p_229092_2_, blockpos)) {
				VoxelShape voxelshape = blockstate.getShape(p_229092_2_, p_229092_3_.below());
				if (!voxelshape.isEmpty()) {
					@SuppressWarnings("deprecation")
					float f = (float) (((double) p_229092_11_ - (p_229092_6_ - (double) p_229092_3_.getY()) / 2.0D)
						* 0.5D * (double) p_229092_2_.getBrightness(p_229092_3_));
					if (f >= 0.0F) {
						if (f > 1.0F) {
							f = 1.0F;
						}

						AABB axisalignedbb = voxelshape.bounds();
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

	private static void shadowVertex(PoseStack.Pose p_229091_0_, VertexConsumer p_229091_1_, float p_229091_2_,
		float p_229091_3_, float p_229091_4_, float p_229091_5_, float p_229091_6_, float p_229091_7_) {
		p_229091_1_.vertex(p_229091_0_.pose(), p_229091_3_, p_229091_4_, p_229091_5_)
			.color(1.0F, 1.0F, 1.0F, p_229091_2_)
			.uv(p_229091_6_, p_229091_7_)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(15728880)
			.normal(p_229091_0_.normal(), 0.0F, 1.0F, 0.0F)
			.endVertex();
	}
}
