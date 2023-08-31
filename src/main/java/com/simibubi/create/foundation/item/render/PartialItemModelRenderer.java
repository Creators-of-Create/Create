package com.simibubi.create.foundation.item.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.render.RenderTypes;

import net.createmod.catnip.utility.Iterate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;

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

	public void render(BakedModel model, int light) {
		render(model, RenderTypes.getItemPartialTranslucent(), light);
	}

	public void renderSolid(BakedModel model, int light) {
		render(model, RenderTypes.getItemPartialSolid(), light);
	}

	public void renderSolidGlowing(BakedModel model, int light) {
		render(model, RenderTypes.getGlowingSolid(), light);
	}

	public void renderGlowing(BakedModel model, int light) {
		render(model, RenderTypes.getGlowingTranslucent(), light);
	}

	public void render(BakedModel model, RenderType type, int light) {
		if (stack.isEmpty())
			return;

		ms.pushPose();
		ms.translate(-0.5D, -0.5D, -0.5D);

		if (!model.isCustomRenderer()) {
			VertexConsumer vc = ItemRenderer.getFoilBufferDirect(buffer, type, true, stack.hasFoil());
			for (BakedModel pass : model.getRenderPasses(stack, false)) {
				renderBakedItemModel(pass, light, ms, vc);
			}
		} else
			IClientItemExtensions.of(stack)
				.getCustomRenderer()
				.renderByItem(stack, transformType, ms, buffer, light, overlay);

		ms.popPose();
	}

	private void renderBakedItemModel(BakedModel model, int light, PoseStack ms, VertexConsumer buffer) {
		ItemRenderer ir = Minecraft.getInstance()
			.getItemRenderer();
		ModelData data = ModelData.EMPTY;

		for (RenderType renderType : model.getRenderTypes(stack, false)) {
			for (Direction direction : Iterate.directions) {
				random.setSeed(42L);
				ir.renderQuadList(ms, buffer, model.getQuads(null, direction, random, data, renderType), stack, light,
					overlay);
			}

			random.setSeed(42L);
			ir.renderQuadList(ms, buffer, model.getQuads(null, null, random, data, renderType), stack, light, overlay);
		}
	}

}
