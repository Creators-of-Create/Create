package com.simibubi.create.content.contraptions.wrench;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.scrollvalue.ScrollValueHandler;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import com.mojang.math.Vector3f;

public class WrenchItemRenderer extends CustomRenderedItemModelRenderer<WrenchModel> {

	public WrenchItemRenderer(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
		super(p_172550_, p_172551_);
	}

	@Override
	protected void render(ItemStack stack, WrenchModel model, PartialItemModelRenderer renderer, ItemTransforms.TransformType transformType,
		PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		renderer.render(model.getOriginalModel(), light);

		float xOffset = -1/16f;
		ms.translate(-xOffset, 0, 0);
		ms.mulPose(Vector3f.YP.rotationDegrees(ScrollValueHandler.getScroll(AnimationTickHolder.getPartialTicks())));
		ms.translate(xOffset, 0, 0);

		renderer.render(model.getPartial("gear"), light);
	}

}
