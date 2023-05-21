package com.simibubi.create.content.equipment.goggles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;

import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.BakedModelWrapper;

public class GogglesModel extends BakedModelWrapper<BakedModel> {

	public GogglesModel(BakedModel template) {
		super(template);
	}

	@Override
	public BakedModel handlePerspective(TransformType cameraTransformType, PoseStack mat) {
		if (cameraTransformType == TransformType.HEAD)
			return AllPartialModels.GOGGLES.get()
				.handlePerspective(cameraTransformType, mat);
		return super.handlePerspective(cameraTransformType, mat);
	}

}
