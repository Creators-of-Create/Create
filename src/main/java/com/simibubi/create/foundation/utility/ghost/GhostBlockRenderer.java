package com.simibubi.create.foundation.utility.ghost;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.simibubi.create.lib.utility.VertexBuilderUtil;

import org.lwjgl.system.MemoryStack;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public abstract class GhostBlockRenderer {

	private static final GhostBlockRenderer transparent = new TransparentGhostBlockRenderer();

	public static GhostBlockRenderer transparent() {
		return transparent;
	}

	private static final GhostBlockRenderer standard = new DefaultGhostBlockRenderer();

	public static GhostBlockRenderer standard() {
		return standard;
	}

	public abstract void render(PoseStack ms, SuperRenderTypeBuffer buffer, GhostBlockParams params);

	private static class DefaultGhostBlockRenderer extends GhostBlockRenderer {

		public void render(PoseStack ms, SuperRenderTypeBuffer buffer, GhostBlockParams params) {
			ms.pushPose();

			BlockRenderDispatcher dispatcher = Minecraft.getInstance()
				.getBlockRenderer();

			BakedModel model = dispatcher.getBlockModel(params.state);

			RenderType layer = ItemBlockRenderTypes.getRenderType(params.state, false);
			VertexConsumer vb = buffer.getEarlyBuffer(layer);

			BlockPos pos = params.pos;
			ms.translate(pos.getX(), pos.getY(), pos.getZ());

			dispatcher.getModelRenderer()
				.renderModel(ms.last(), vb, params.state, model, 1f, 1f, 1f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);

			ms.popPose();
		}

	}

	private static class TransparentGhostBlockRenderer extends GhostBlockRenderer {

		public void render(PoseStack ms, SuperRenderTypeBuffer buffer, GhostBlockParams params) {

			// prepare
			ms.pushPose();

			// RenderSystem.pushMatrix();

			Minecraft mc = Minecraft.getInstance();
			BlockRenderDispatcher dispatcher = mc.getBlockRenderer();

			BakedModel model = dispatcher.getBlockModel(params.state);

			// RenderType layer = RenderTypeLookup.getEntityBlockLayer(params.state);
			RenderType layer = RenderType.translucent();
			VertexConsumer vb = buffer.getEarlyBuffer(layer);

			BlockPos pos = params.pos;
			ms.translate(pos.getX(), pos.getY(), pos.getZ());

			ms.translate(.5, .5, .5);
			ms.scale(.85f, .85f, .85f);
			ms.translate(-.5, -.5, -.5);

			// dispatcher.getBlockModelRenderer().renderModel(ms.peek(), vb, params.state, model, 1f, 1f, 1f, LightTexture.FULL_BRIGHT, OverlayTexture.DEFAULT_UV, VirtualEmptyModelData.INSTANCE);
			renderModel(params, ms.last(), vb, params.state, model, 1f, 1f, 1f,
				LevelRenderer.getLightColor(mc.level, pos), OverlayTexture.NO_OVERLAY);

			// buffer.draw();
			// clean
			// RenderSystem.popMatrix();
			ms.popPose();

		}

		// BlockModelRenderer
		public void renderModel(GhostBlockParams params, PoseStack.Pose entry, VertexConsumer vb,
			@Nullable BlockState state, BakedModel model, float p_228804_5_, float p_228804_6_, float p_228804_7_,
			int p_228804_8_, int p_228804_9_) {
			Random random = new Random();

			for (Direction direction : Direction.values()) {
				random.setSeed(42L);
				renderQuad(params, entry, vb, p_228804_5_, p_228804_6_, p_228804_7_,
					model.getQuads(state, direction, random), p_228804_8_, p_228804_9_);
			}

			random.setSeed(42L);
			renderQuad(params, entry, vb, p_228804_5_, p_228804_6_, p_228804_7_,
				model.getQuads(state, (Direction) null, random), p_228804_8_, p_228804_9_);
		}

		// BlockModelRenderer
		private static void renderQuad(GhostBlockParams params, PoseStack.Pose p_228803_0_,
			VertexConsumer p_228803_1_, float p_228803_2_, float p_228803_3_, float p_228803_4_,
			List<BakedQuad> p_228803_5_, int p_228803_6_, int p_228803_7_) {
			Float alpha = params.alphaSupplier.get() * .75f * PlacementHelpers.getCurrentAlpha();

			for (BakedQuad bakedquad : p_228803_5_) {
				float f;
				float f1;
				float f2;
				if (bakedquad.isTinted()) {
					f = Mth.clamp(p_228803_2_, 0.0F, 1.0F);
					f1 = Mth.clamp(p_228803_3_, 0.0F, 1.0F);
					f2 = Mth.clamp(p_228803_4_, 0.0F, 1.0F);
				} else {
					f = 1.0F;
					f1 = 1.0F;
					f2 = 1.0F;
				}

				quad(alpha, p_228803_1_, p_228803_0_, bakedquad, new float[] { 1f, 1f, 1f, 1f }, f, f1, f2,
					new int[] { p_228803_6_, p_228803_6_, p_228803_6_, p_228803_6_ }, p_228803_7_);
			}

		}

		// IVertexBuilder
		static void quad(float alpha, VertexConsumer vb, PoseStack.Pose p_227890_1_, BakedQuad p_227890_2_,
			float[] p_227890_3_, float p_227890_4_, float p_227890_5_, float p_227890_6_, int[] p_227890_7_,
			int p_227890_8_) {
			int[] aint = p_227890_2_.getVertices();
			Vec3i Vector3i = p_227890_2_.getDirection()
				.getNormal();
			Vector3f vector3f = new Vector3f((float) Vector3i.getX(), (float) Vector3i.getY(), (float) Vector3i.getZ());
			Matrix4f matrix4f = p_227890_1_.pose();
			vector3f.transform(p_227890_1_.normal());
			int vertexSize = DefaultVertexFormat.BLOCK.getIntegerSize();
			int j = aint.length / vertexSize;

			try (MemoryStack memorystack = MemoryStack.stackPush()) {
				ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
				IntBuffer intbuffer = bytebuffer.asIntBuffer();

				for (int k = 0; k < j; ++k) {
					((Buffer) intbuffer).clear();
					intbuffer.put(aint, k * vertexSize, vertexSize);
					float f = bytebuffer.getFloat(0);
					float f1 = bytebuffer.getFloat(4);
					float f2 = bytebuffer.getFloat(8);
					float r;
					float g;
					float b;

					r = p_227890_3_[k] * p_227890_4_;
					g = p_227890_3_[k] * p_227890_5_;
					b = p_227890_3_[k] * p_227890_6_;

					int l = VertexBuilderUtil.applyBakedLighting(p_227890_7_[k], bytebuffer);
					float f9 = bytebuffer.getFloat(16);
					float f10 = bytebuffer.getFloat(20);
					Vector4f vector4f = new Vector4f(f, f1, f2, 1.0F);
					vector4f.transform(matrix4f);
					VertexBuilderUtil.applyBakedNormals(vector3f, bytebuffer, p_227890_1_.normal());
					vb.vertex(vector4f.x(), vector4f.y(), vector4f.z(), r, g, b, alpha, f9, f10, p_227890_8_,
						l, vector3f.x(), vector3f.y(), vector3f.z());
				}
			}
		}

	}

}
