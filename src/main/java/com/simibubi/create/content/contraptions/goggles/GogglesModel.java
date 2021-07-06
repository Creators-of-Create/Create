package com.simibubi.create.content.contraptions.goggles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraftforge.client.model.BakedModelWrapper;

public class GogglesModel extends BakedModelWrapper<IBakedModel> {

	public GogglesModel(IBakedModel template) {
		super(template);
	}

	@Override
	public IBakedModel handlePerspective(TransformType cameraTransformType, MatrixStack mat) {
		if (cameraTransformType == TransformType.HEAD)
			return AllBlockPartials.GOGGLES.get()
				.handlePerspective(cameraTransformType, mat);
		return super.handlePerspective(cameraTransformType, mat);
	}

}
