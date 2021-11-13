package com.simibubi.create.lib.helper;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.lib.mixin.accessor.ItemRendererAccessor;
import com.simibubi.create.lib.utility.MixinHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class ItemRendererHelper {
	public static void renderModelLists(ItemRenderer renderer, BakedModel model, ItemStack stack, int light, int overlay, PoseStack matrices, VertexConsumer vertices) {
		get(renderer).create$renderModelLists(model, stack, light, overlay, matrices, vertices);
	}

	public static void renderQuadList(ItemRenderer renderer, PoseStack matrices, VertexConsumer vertices, List<BakedQuad> quads, ItemStack stack, int light, int overlay) {
		get(renderer).create$renderQuadList(matrices, vertices, quads, stack, light, overlay);
	}

	public static TextureManager getTextureManager(ItemRenderer renderer) {
		return get(renderer).create$getTextureManager();
	}

	private static ItemRendererAccessor get(ItemRenderer renderer) {
		return MixinHelper.cast(renderer);
	}

	private ItemRendererHelper() {}
}
