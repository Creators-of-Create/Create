package com.simibubi.create.foundation.item.render;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.simibubi.create.foundation.render.RenderTypes;
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

	private static final PartialItemModelRenderer INSTANCE = new PartialItemModelRenderer();

	private final Random random = new Random();

	private ItemStack stack;
	private ItemCameraTransforms.TransformType transformType;
	private MatrixStack ms;
	private IRenderTypeBuffer buffer;
	private int overlay;

	public static PartialItemModelRenderer of(ItemStack stack, ItemCameraTransforms.TransformType transformType,
		MatrixStack ms, IRenderTypeBuffer buffer, int overlay) {
		PartialItemModelRenderer instance = INSTANCE;
		instance.stack = stack;
		instance.transformType = transformType;
		instance.ms = ms;
		instance.buffer = buffer;
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

		ms.pushPose();
		ms.translate(-0.5D, -0.5D, -0.5D);

		if (!model.isCustomRenderer())
			renderBakedItemModel(model, light, ms,
				ItemRenderer.getFoilBufferDirect(buffer, type, true, stack.hasFoil()));
		else
			stack.getItem()
				.getItemStackTileEntityRenderer()
				.renderByItem(stack, transformType, ms, buffer, light, overlay);

		ms.popPose();
	}

	private void renderBakedItemModel(IBakedModel model, int light, MatrixStack ms, IVertexBuilder buffer) {
		ItemRenderer ir = Minecraft.getInstance()
			.getItemRenderer();
		IModelData data = EmptyModelData.INSTANCE;

		for (Direction direction : Iterate.directions) {
			random.setSeed(42L);
			ir.renderQuadList(ms, buffer, model.getQuads(null, direction, random, data), stack, light,
				overlay);
		}

		random.setSeed(42L);
		ir.renderQuadList(ms, buffer, model.getQuads(null, null, random, data), stack, light, overlay);
	}

}
