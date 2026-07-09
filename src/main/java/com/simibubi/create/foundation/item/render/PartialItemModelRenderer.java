package com.simibubi.create.foundation.item.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.render.RenderTypes;

import net.createmod.catnip.api.data.Iterate;
import net.minecraft.client.Minecraft;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.model.data.ModelData;

public class PartialItemModelRenderer {

	private static final PartialItemModelRenderer INSTANCE = new PartialItemModelRenderer();

	private final RandomSource random = RandomSource.create();

	private ItemStack stack;
	private ItemDisplayContext transformType;
	private PoseStack ms;
	private MultiBufferSource buffer;
	private int overlay;

	public static PartialItemModelRenderer of(ItemStack stack, ItemDisplayContext transformType,
		PoseStack ms, MultiBufferSource buffer, int overlay) {
		PartialItemModelRenderer instance = INSTANCE;
		instance.stack = stack;
		instance.transformType = transformType;
		instance.ms = ms;
		instance.buffer = buffer;
		instance.overlay = overlay;
		return instance;
	}

	public void render(Object model, int light) {
		render(model, (RenderType) null, light);
	}

	public void renderSolid(Object model, int light) {
		render(model, (RenderType) null, light);
	}

	public void renderGlowing(Object model, int light) {
		render(model, RenderTypes.itemGlowingTranslucent(), light);
	}

	public void renderSolidGlowing(Object model, int light) {
		render(model, RenderTypes.itemGlowingSolid(), light);
	}

	public void render(Object model, RenderType type, int light) {
		// TODO 26.2: Rebuild partial item model rendering on top of ItemModel.
	}

	private void renderBakedItemModel(Object model, int light, PoseStack ms, VertexConsumer buffer) {
	}

}
