package com.simibubi.create.content.contraptions.debrisShield;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueHandler;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;

public class DebrisShieldItemRenderer extends CustomRenderedItemModelRenderer<DebrisShieldModel> {

	@Override
	protected void render(ItemStack stack, DebrisShieldModel model, PartialItemModelRenderer renderer, ItemTransforms.TransformType transformType,
                          PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		renderer.render(model.getOriginalModel(), light);

		float xOffset = -1/16f;
		ms.translate(-xOffset, 0, 0);
		ms.mulPose(Vector3f.YP.rotationDegrees(ScrollValueHandler.getScroll(AnimationTickHolder.getPartialTicks())));
		ms.translate(xOffset, 0, 0);

		renderer.render(model.getPartial("gear"), light);
	}

	@Override
	public DebrisShieldModel createModel(BakedModel originalModel) {
		return new DebrisShieldModel(originalModel);
	}

}
