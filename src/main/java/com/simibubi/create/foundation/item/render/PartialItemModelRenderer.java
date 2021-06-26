package com.simibubi.create.foundation.item.render;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.foundation.renderState.RenderTypes;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

public class PartialItemModelRenderer {

	static PartialItemModelRenderer instance;

	ItemStack stack;
	int overlay;
	MatrixStack ms;
	ItemCameraTransforms.TransformType transformType;
	IRenderTypeBuffer buffer;

	static PartialItemModelRenderer get() {
		if (instance == null)
			instance = new PartialItemModelRenderer();
		return instance;
	}

	public static PartialItemModelRenderer of(ItemStack stack, ItemCameraTransforms.TransformType transformType,
		MatrixStack ms, IRenderTypeBuffer buffer, int overlay) {
		PartialItemModelRenderer instance = get();
		instance.stack = stack;
		instance.buffer = buffer;
		instance.ms = ms;
		instance.transformType = transformType;
		instance.overlay = overlay;
		return instance;
	}

	public void render(IBakedModel model, int light) {
		render(model, RenderTypes.getItemPartialTranslucent(), light);
	}

	public void renderSolid(IBakedModel model, int light) {
		render(model, RenderTypes.getItemPartialSolid(), light);
	}

	public void renderSolidGlowing(IBakedModel model, int light) {
		render(model, RenderTypes.getGlowingSolid(), light);
	}

	public void renderGlowing(IBakedModel model, int light) {
		render(model, RenderTypes.getGlowingTranslucent(), light);
	}

	public void render(IBakedModel model, RenderType type, int light) {
		if (stack.isEmpty())
			return;

		ms.push();
		ms.translate(-0.5D, -0.5D, -0.5D);

		if (!model.isBuiltInRenderer())
			renderBakedItemModel(model, light, ms,
				ItemRenderer.getArmorVertexConsumer(buffer, type, true, stack.hasEffect()));
		else
			stack.getItem()
				.getItemStackTileEntityRenderer()
				.render(stack, transformType, ms, buffer, light, overlay);

		ms.pop();
	}

	private void renderBakedItemModel(IBakedModel model, int light, MatrixStack ms, IVertexBuilder p_229114_6_) {
		ItemRenderer ir = Minecraft.getInstance()
			.getItemRenderer();
		Random random = new Random();
		IModelData data = EmptyModelData.INSTANCE;

		for (Direction direction : Iterate.directions) {
			random.setSeed(42L);
			ir.renderBakedItemQuads(ms, p_229114_6_, model.getQuads(null, direction, random, data), stack, light,
				overlay);
		}

		random.setSeed(42L);
		ir.renderBakedItemQuads(ms, p_229114_6_, model.getQuads(null, null, random, data), stack, light, overlay);
	}

}
