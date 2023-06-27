package com.simibubi.create.foundation.item.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.BakedModelWrapper;

public class CustomRenderedItemModel extends BakedModelWrapper<BakedModel> {

	public CustomRenderedItemModel(BakedModel originalModel) {
		super(originalModel);
	}

	@Override
	public boolean isCustomRenderer() {
		return true;
	}

	@Override
	public BakedModel applyTransform(ItemTransforms.TransformType cameraTransformType, PoseStack mat,
		boolean leftHand) {
		// Super call returns originalModel, but we want to return this, else BEWLR
		// won't be used.
		super.applyTransform(cameraTransformType, mat, leftHand);
		return this;
	}

	public BakedModel getOriginalModel() {
		return originalModel;
	}

}
