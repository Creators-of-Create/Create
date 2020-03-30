package com.simibubi.create.modules.contraptions;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;

public class WrenchItemRenderer extends ItemStackTileEntityRenderer {

	@Override
	public void render(ItemStack stack, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		WrenchModel mainModel = (WrenchModel) itemRenderer.getItemModelWithOverrides(stack, Minecraft.getInstance().world, Minecraft.getInstance().player);
		float worldTime = AnimationTickHolder.getRenderTick();

		ms.push();
		ms.translate(0.5F, 0.5F, 0.5F);
		itemRenderer.renderItem(stack, TransformType.NONE, false, ms, buffer, light, overlay, mainModel.getBakedModel());

		float angle = worldTime * -.5f % 360;
		
		float xOffset = -1/32f;
		float zOffset = 0;
		ms.translate(-xOffset, 0, -zOffset);
		ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(angle));
		ms.translate(xOffset, 0, zOffset);
		
		itemRenderer.renderItem(stack, TransformType.NONE, false, ms, buffer, light, overlay, mainModel.getPartial("gear"));

		ms.pop();
	}

}
