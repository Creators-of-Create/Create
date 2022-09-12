package com.simibubi.create.foundation.item.render;

import java.util.HashSet;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.RenderProperties;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class CustomRenderedItemModelRenderer<M extends CustomRenderedItemModel> extends BlockEntityWithoutLevelRenderer {

	private static final Set<Item> ITEMS = new HashSet<>();

	public CustomRenderedItemModelRenderer() {
		super(null, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		M mainModel = (M) Minecraft.getInstance()
			.getItemRenderer()
			.getModel(stack, null, null, 0);
		PartialItemModelRenderer renderer = PartialItemModelRenderer.of(stack, transformType, ms, buffer, overlay);

		ms.pushPose();
		ms.translate(0.5F, 0.5F, 0.5F);
		render(stack, mainModel, renderer, transformType, ms, buffer, light, overlay);
		ms.popPose();
	}

	protected abstract void render(ItemStack stack, M model, PartialItemModelRenderer renderer, ItemTransforms.TransformType transformType,
		PoseStack ms, MultiBufferSource buffer, int light, int overlay);

	public abstract M createModel(BakedModel originalModel);

	/**
	 * Track an item that uses a subclass of {@link CustomRenderedItemModelRenderer} as its custom renderer
	 * to automatically register {@link #createModel} to {@link CustomRenderedItems} on client setup so that
	 * its model can be swapped.
	 * @param item The item that should have its model swapped.
	 */
	public static void registerForSwapping(Item item) {
		ITEMS.add(item);
	}

	public static void acceptModelFuncs(NonNullBiConsumer<Item, NonNullFunction<BakedModel, ? extends CustomRenderedItemModel>> consumer) {
		for (Item item : ITEMS) {
			if (ForgeRegistries.ITEMS.containsValue(item)) {
				BlockEntityWithoutLevelRenderer renderer = RenderProperties.get(item).getItemStackRenderer();
				if (renderer instanceof CustomRenderedItemModelRenderer<?> customRenderer) {
					consumer.accept(item, customRenderer::createModel);
				}
			}
		}
	}

}
