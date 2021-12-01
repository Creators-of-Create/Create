package com.simibubi.create.lib.render;

import java.util.Random;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.RenderMaterialImpl;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class ModelRenderingUtil {
	private static final ThreadLocal<LayerSpecificModelWrapper> LAYER_WRAPPER = ThreadLocal.withInitial(LayerSpecificModelWrapper::new);

	public static void emitBlockQuadsChecked(BakedModel model, BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		if (((FabricBakedModel) model).isVanillaAdapter()) {
			context.fallbackConsumer().accept(model);
		} else {
			((FabricBakedModel) model).emitBlockQuads(blockView, state, pos, randomSupplier, context);
		}
	}

	public static void renderForLayer(ModelBlockRenderer blockModelRenderer, BlockAndTintGetter blockDisplayReader, BakedModel bakedModel, BlockState blockState, BlockPos blockPos, PoseStack matrixStack, VertexConsumer vertexBuilder, boolean bl, Random random, long l, int i, RenderType renderLayer) {
		BakedModel model = bakedModel;
		if (!((FabricBakedModel) bakedModel).isVanillaAdapter()) {
			LayerSpecificModelWrapper wrapper = LAYER_WRAPPER.get();
			wrapper.setWrapped(bakedModel);
			wrapper.setLayer(renderLayer);
			model = wrapper;
		}
		blockModelRenderer.tesselateBlock(blockDisplayReader, model, blockState, blockPos, matrixStack, vertexBuilder, bl, random, l, i);
	}

	// Because RenderMaterial does not expose the BlendMode, we have to rely on implementation details here.
	// Currently Indigo is supported. Indium and FREX will be supported in the future.
	public static BlendMode getBlendMode(RenderMaterial material) {
		BlendMode mode = BlendMode.DEFAULT;
		if (material instanceof RenderMaterialImpl) {
			RenderMaterialImpl impl = (RenderMaterialImpl) material;
			mode = impl.blendMode(0);
		}
		return mode;
	}

	private ModelRenderingUtil() {}

	private static class LayerSpecificModelWrapper extends ForwardingBakedModel {
		private RenderType layer;

		public void setWrapped(BakedModel wrapped) {
			this.wrapped = wrapped;
		}

		public void setLayer(RenderType layer) {
			this.layer = layer;
		}

		@Override
		public boolean isVanillaAdapter() {
			return false;
		}

		@Override
		public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
			RenderType defaultLayer = ItemBlockRenderTypes.getChunkRenderType(state);
			context.pushTransform(quad -> {
				RenderType l = getBlendMode(quad.material()).blockRenderLayer;
				if (l == null) {
					l = defaultLayer;
				}
				return l == layer;
			});
			super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
			context.popTransform();
		}
	}
}
