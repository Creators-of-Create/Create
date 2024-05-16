package com.simibubi.create.foundation.utility.ghost;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.placement.PlacementHelpers;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.render.VirtualRenderHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

public abstract class GhostBlockRenderer {

	private static final GhostBlockRenderer STANDARD = new DefaultGhostBlockRenderer();

	public static GhostBlockRenderer standard() {
		return STANDARD;
	}

	private static final GhostBlockRenderer TRANSPARENT = new TransparentGhostBlockRenderer();

	public static GhostBlockRenderer transparent() {
		return TRANSPARENT;
	}

	public abstract void render(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, GhostBlockParams params);

	private static class DefaultGhostBlockRenderer extends GhostBlockRenderer {

		@Override
		public void render(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, GhostBlockParams params) {
			ms.pushPose();
			BlockRenderDispatcher dispatcher = Minecraft.getInstance()
				.getBlockRenderer();
			ModelBlockRenderer renderer = dispatcher.getModelRenderer();

			BlockState state = params.state;
			BlockPos pos = params.pos;

			BakedModel model = dispatcher.getBlockModel(state);

			ms.pushPose();
			ms.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);

			for (RenderType layer : model.getRenderTypes(state, RandomSource.create(42L), VirtualRenderHelper.VIRTUAL_DATA)) {
				VertexConsumer vb = buffer.getEarlyBuffer(layer);
				renderer.renderModel(ms.last(), vb, state, model, 1f, 1f, 1f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
					VirtualRenderHelper.VIRTUAL_DATA, layer);
			}

			ms.popPose();
		}

	}

	private static class TransparentGhostBlockRenderer extends GhostBlockRenderer {

		@Override
		public void render(PoseStack ms, SuperRenderTypeBuffer buffer, Vec3 camera, GhostBlockParams params) {
			ms.pushPose();

			Minecraft mc = Minecraft.getInstance();
			BlockRenderDispatcher dispatcher = mc.getBlockRenderer();

			BlockState state = params.state;
			BlockPos pos = params.pos;
			float alpha = params.alphaSupplier.get() * .75f * PlacementHelpers.getCurrentAlpha();

			BakedModel model = dispatcher.getBlockModel(state);
			RenderType layer = RenderType.translucent();
			VertexConsumer vb = buffer.getEarlyBuffer(layer);

			ms.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
			ms.translate(.5, .5, .5);
			ms.scale(.85f, .85f, .85f);
			ms.translate(-.5, -.5, -.5);

			renderModel(ms.last(), vb, state, model, 1f, 1f, 1f, alpha,
				LevelRenderer.getLightColor(mc.level, pos), OverlayTexture.NO_OVERLAY,
				VirtualRenderHelper.VIRTUAL_DATA, layer);

			ms.popPose();
		}

		// ModelBlockRenderer
		public void renderModel(PoseStack.Pose pose, VertexConsumer consumer,
			@Nullable BlockState state, BakedModel model, float red, float green, float blue,
			float alpha, int packedLight, int packedOverlay, ModelData modelData, RenderType renderType) {
			RandomSource random = RandomSource.create();

			for (Direction direction : Direction.values()) {
				random.setSeed(42L);
				renderQuadList(pose, consumer, red, green, blue, alpha,
					model.getQuads(state, direction, random, modelData, null), packedLight, packedOverlay);
			}

			random.setSeed(42L);
			renderQuadList(pose, consumer, red, green, blue, alpha,
				model.getQuads(state, null, random, modelData, null), packedLight, packedOverlay);
		}

		// ModelBlockRenderer
		private static void renderQuadList(PoseStack.Pose pose, VertexConsumer consumer,
			float red, float green, float blue, float alpha, List<BakedQuad> quads,
			int packedLight, int packedOverlay) {
			for (BakedQuad quad : quads) {
				float f;
				float f1;
				float f2;
				if (quad.isTinted()) {
					f = Mth.clamp(red, 0.0F, 1.0F);
					f1 = Mth.clamp(green, 0.0F, 1.0F);
					f2 = Mth.clamp(blue, 0.0F, 1.0F);
				} else {
					f = 1.0F;
					f1 = 1.0F;
					f2 = 1.0F;
				}

				consumer.putBulkData(pose, quad, f, f1, f2, alpha, packedLight, packedOverlay, true);
			}

		}

	}

}
