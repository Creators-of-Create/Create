package com.simibubi.create.foundation.utility.ghost;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import org.lwjgl.system.MemoryStack;

import com.jozufozu.flywheel.util.VirtualEmptyModelData;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.math.vector.Vector4f;

public abstract class GhostBlockRenderer {

	private static final GhostBlockRenderer transparent = new TransparentGhostBlockRenderer();

	public static GhostBlockRenderer transparent() {
		return transparent;
	}

	private static final GhostBlockRenderer standard = new DefaultGhostBlockRenderer();

	public static GhostBlockRenderer standard() {
		return standard;
	}

	public abstract void render(MatrixStack ms, SuperRenderTypeBuffer buffer, GhostBlockParams params);

	private static class DefaultGhostBlockRenderer extends GhostBlockRenderer {

		public void render(MatrixStack ms, SuperRenderTypeBuffer buffer, GhostBlockParams params) {
			ms.pushPose();

			BlockRendererDispatcher dispatcher = Minecraft.getInstance()
				.getBlockRenderer();

			IBakedModel model = dispatcher.getBlockModel(params.state);

			RenderType layer = RenderTypeLookup.getRenderType(params.state, false);
			IVertexBuilder vb = buffer.getEarlyBuffer(layer);

			BlockPos pos = params.pos;
			ms.translate(pos.getX(), pos.getY(), pos.getZ());

			dispatcher.getModelRenderer()
				.renderModel(ms.last(), vb, params.state, model, 1f, 1f, 1f, 0xF000F0, OverlayTexture.NO_OVERLAY,
					VirtualEmptyModelData.INSTANCE);

			ms.popPose();
		}

	}

	private static class TransparentGhostBlockRenderer extends GhostBlockRenderer {

		public void render(MatrixStack ms, SuperRenderTypeBuffer buffer, GhostBlockParams params) {

			// prepare
			ms.pushPose();

			// RenderSystem.pushMatrix();

			Minecraft mc = Minecraft.getInstance();
			BlockRendererDispatcher dispatcher = mc.getBlockRenderer();

			IBakedModel model = dispatcher.getBlockModel(params.state);

			// RenderType layer = RenderTypeLookup.getEntityBlockLayer(params.state);
			RenderType layer = RenderType.translucent();
			IVertexBuilder vb = buffer.getEarlyBuffer(layer);

			BlockPos pos = params.pos;
			ms.translate(pos.getX(), pos.getY(), pos.getZ());

			ms.translate(.5, .5, .5);
			ms.scale(.85f, .85f, .85f);
			ms.translate(-.5, -.5, -.5);

			// dispatcher.getBlockModelRenderer().renderModel(ms.peek(), vb, params.state, model, 1f, 1f, 1f, 0xF000F0, OverlayTexture.DEFAULT_UV, VirtualEmptyModelData.INSTANCE);
			renderModel(params, ms.last(), vb, params.state, model, 1f, 1f, 1f,
				WorldRenderer.getLightColor(mc.level, pos), OverlayTexture.NO_OVERLAY,
				VirtualEmptyModelData.INSTANCE);

			// buffer.draw();
			// clean
			// RenderSystem.popMatrix();
			ms.popPose();

		}

		// BlockModelRenderer
		public void renderModel(GhostBlockParams params, MatrixStack.Entry entry, IVertexBuilder vb,
			@Nullable BlockState state, IBakedModel model, float p_228804_5_, float p_228804_6_, float p_228804_7_,
			int p_228804_8_, int p_228804_9_, net.minecraftforge.client.model.data.IModelData modelData) {
			Random random = new Random();

			for (Direction direction : Direction.values()) {
				random.setSeed(42L);
				renderQuad(params, entry, vb, p_228804_5_, p_228804_6_, p_228804_7_,
					model.getQuads(state, direction, random, modelData), p_228804_8_, p_228804_9_);
			}

			random.setSeed(42L);
			renderQuad(params, entry, vb, p_228804_5_, p_228804_6_, p_228804_7_,
				model.getQuads(state, (Direction) null, random, modelData), p_228804_8_, p_228804_9_);
		}

		// BlockModelRenderer
		private static void renderQuad(GhostBlockParams params, MatrixStack.Entry p_228803_0_,
			IVertexBuilder p_228803_1_, float p_228803_2_, float p_228803_3_, float p_228803_4_,
			List<BakedQuad> p_228803_5_, int p_228803_6_, int p_228803_7_) {
			Float alpha = params.alphaSupplier.get() * .75f * PlacementHelpers.getCurrentAlpha();

			for (BakedQuad bakedquad : p_228803_5_) {
				float f;
				float f1;
				float f2;
				if (bakedquad.isTinted()) {
					f = MathHelper.clamp(p_228803_2_, 0.0F, 1.0F);
					f1 = MathHelper.clamp(p_228803_3_, 0.0F, 1.0F);
					f2 = MathHelper.clamp(p_228803_4_, 0.0F, 1.0F);
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
		static void quad(float alpha, IVertexBuilder vb, MatrixStack.Entry p_227890_1_, BakedQuad p_227890_2_,
			float[] p_227890_3_, float p_227890_4_, float p_227890_5_, float p_227890_6_, int[] p_227890_7_,
			int p_227890_8_) {
			int[] aint = p_227890_2_.getVertices();
			Vector3i Vector3i = p_227890_2_.getDirection()
				.getNormal();
			Vector3f vector3f = new Vector3f((float) Vector3i.getX(), (float) Vector3i.getY(), (float) Vector3i.getZ());
			Matrix4f matrix4f = p_227890_1_.pose();
			vector3f.transform(p_227890_1_.normal());
			int vertexSize = DefaultVertexFormats.BLOCK.getIntegerSize();
			int j = aint.length / vertexSize;

			try (MemoryStack memorystack = MemoryStack.stackPush()) {
				ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormats.BLOCK.getVertexSize());
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

					int l = vb.applyBakedLighting(p_227890_7_[k], bytebuffer);
					float f9 = bytebuffer.getFloat(16);
					float f10 = bytebuffer.getFloat(20);
					Vector4f vector4f = new Vector4f(f, f1, f2, 1.0F);
					vector4f.transform(matrix4f);
					vb.applyBakedNormals(vector3f, bytebuffer, p_227890_1_.normal());
					vb.vertex(vector4f.x(), vector4f.y(), vector4f.z(), r, g, b, alpha, f9, f10, p_227890_8_,
						l, vector3f.x(), vector3f.y(), vector3f.z());
				}
			}
		}

	}

}
