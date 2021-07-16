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
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
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
			@Nullable BlockState state, IBakedModel model, float pRed, float pGreen, float pBlue,
			int pCombinedLightIn, int pCombinedOverlayIn, net.minecraftforge.client.model.data.IModelData modelData) {
			Random random = new Random();

			for (Direction direction : Direction.values()) {
				random.setSeed(42L);
				renderQuad(params, entry, vb, pRed, pGreen, pBlue,
					model.getQuads(state, direction, random, modelData), pCombinedLightIn, pCombinedOverlayIn);
			}

			random.setSeed(42L);
			renderQuad(params, entry, vb, pRed, pGreen, pBlue,
				model.getQuads(state, (Direction) null, random, modelData), pCombinedLightIn, pCombinedOverlayIn);
		}

		// BlockModelRenderer
		private static void renderQuad(GhostBlockParams params, MatrixStack.Entry pMatrixEntry,
			IVertexBuilder pBuffer, float pRed, float pGreen, float pBlue,
			List<BakedQuad> pListQuads, int pCombinedLightIn, int pCombinedOverlayIn) {
			float alpha = params.alphaSupplier.get() * .75f * PlacementHelpers.getCurrentAlpha();

			for (BakedQuad bakedquad : pListQuads) {
				float f;
				float f1;
				float f2;
				if (bakedquad.isTinted()) {
					f = MathHelper.clamp(pRed, 0.0F, 1.0F);
					f1 = MathHelper.clamp(pGreen, 0.0F, 1.0F);
					f2 = MathHelper.clamp(pBlue, 0.0F, 1.0F);
				} else {
					f = 1.0F;
					f1 = 1.0F;
					f2 = 1.0F;
				}

				quad(alpha, pBuffer, pMatrixEntry, bakedquad, new float[] { 1f, 1f, 1f, 1f }, f, f1, f2,
					new int[] { pCombinedLightIn, pCombinedLightIn, pCombinedLightIn, pCombinedLightIn }, pCombinedOverlayIn);
			}

		}

		// IVertexBuilder
		static void quad(float alpha, IVertexBuilder vb, MatrixStack.Entry pMatrixEntryIn, BakedQuad pQuadIn,
			float[] pColorMuls, float pRedIn, float pGreenIn, float pBlueIn, int[] pCombinedLightsIn,
			int pCombinedOverlayIn) {
			int[] aint = pQuadIn.getVertices();
			Vector3i Vector3i = pQuadIn.getDirection()
				.getNormal();
			Vector3f vector3f = new Vector3f((float) Vector3i.getX(), (float) Vector3i.getY(), (float) Vector3i.getZ());
			Matrix4f matrix4f = pMatrixEntryIn.pose();
			vector3f.transform(pMatrixEntryIn.normal());
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

					r = pColorMuls[k] * pRedIn;
					g = pColorMuls[k] * pGreenIn;
					b = pColorMuls[k] * pBlueIn;

					int l = vb.applyBakedLighting(pCombinedLightsIn[k], bytebuffer);
					float f9 = bytebuffer.getFloat(16);
					float f10 = bytebuffer.getFloat(20);
					Vector4f vector4f = new Vector4f(f, f1, f2, 1.0F);
					vector4f.transform(matrix4f);
					vb.applyBakedNormals(vector3f, bytebuffer, pMatrixEntryIn.normal());
					vb.vertex(vector4f.x(), vector4f.y(), vector4f.z(), r, g, b, alpha, f9, f10, pCombinedOverlayIn,
						l, vector3f.x(), vector3f.y(), vector3f.z());
				}
			}
		}

	}

}
