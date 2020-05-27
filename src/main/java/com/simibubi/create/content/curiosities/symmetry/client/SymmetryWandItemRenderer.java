package com.simibubi.create.content.curiosities.symmetry.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class SymmetryWandItemRenderer extends ItemStackTileEntityRenderer {

	@Override
	public void render(ItemStack stack, MatrixStack ms, IRenderTypeBuffer buffer, int light, int overlay) {

		ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
		SymmetryWandModel mainModel = (SymmetryWandModel) itemRenderer.getItemModelWithOverrides(stack, null, null);
		float worldTime = AnimationTickHolder.getRenderTick() / 20;

		ms.push();
		ms.translate(0.5F, 0.5F, 0.5F);
		itemRenderer.renderItem(stack, TransformType.NONE, false, ms, buffer, light, overlay, mainModel.getBakedModel());
		itemRenderer.renderItem(stack, TransformType.NONE, false, ms, buffer, 0xF000F0, overlay, mainModel.getPartial("core"));

		float floating = MathHelper.sin(worldTime) * .05f;
		ms.translate(0, floating, 0);
		float angle = worldTime * -10 % 360;
		ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(angle));
		itemRenderer.renderItem(stack, TransformType.NONE, false, ms, buffer, 0xF000F0, overlay, mainModel.getPartial("bits"));

		ms.pop();
	}

}
