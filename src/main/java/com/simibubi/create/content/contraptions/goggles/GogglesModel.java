package com.simibubi.create.content.contraptions.goggles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlockPartials;

import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.BakedModelWrapper;

public class GogglesModel extends BakedModelWrapper<BakedModel> {

	public GogglesModel(BakedModel template) {
		super(template);
	}

	@Override
	public BakedModel applyTransform(TransformType cameraTransformType, PoseStack mat, boolean leftHanded) {
		if (cameraTransformType == TransformType.HEAD)
			return AllBlockPartials.GOGGLES.get()
				.applyTransform(cameraTransformType, mat, leftHanded);
		return super.applyTransform(cameraTransformType, mat, leftHanded);
	}

}
