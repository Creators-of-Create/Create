package com.simibubi.create.foundation.utility.ghost;

import java.util.Random;

import com.jozufozu.flywheel.core.virtual.VirtualEmptyBlockGetter;
import com.jozufozu.flywheel.fabric.model.DefaultLayerFilteringBakedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.placement.PlacementHelpers;
import io.github.fabricators_of_create.porting_lib.render.FixedLightBakedModel;
import io.github.fabricators_of_create.porting_lib.render.TranslucentBakedModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;

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

//			dispatcher.getModelRenderer()
//				.renderModel(ms.last(), vb, params.state, model, 1f, 1f, 1f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
			model = DefaultLayerFilteringBakedModel.wrap(model);
			dispatcher.getModelRenderer()
				.tesselateBlock(VirtualEmptyBlockGetter.FULL_BRIGHT, model, params.state, pos, ms, vb, false, new Random(), 42L, OverlayTexture.NO_OVERLAY);

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
//			dispatcher.getModelRenderer()
//				.renderModel(ms.last(), vb, params.state, model, 1f, 1f, 1f,
//					LevelRenderer.getLightColor(mc.level, pos), OverlayTexture.NO_OVERLAY);
			model = DefaultLayerFilteringBakedModel.wrap(model);
			model = FixedLightBakedModel.wrap(model, LevelRenderer.getLightColor(mc.level, pos));
			model = TranslucentBakedModel.wrap(model, () -> params.alphaSupplier.get() * .75f * PlacementHelpers.getCurrentAlpha());
			dispatcher.getModelRenderer()
				.tesselateBlock(VirtualEmptyBlockGetter.INSTANCE, model, params.state, pos, ms, vb, false, new Random(), 42L, OverlayTexture.NO_OVERLAY);

			// buffer.draw();
			// clean
			// RenderSystem.popMatrix();
			ms.popPose();

		}

	}

}
