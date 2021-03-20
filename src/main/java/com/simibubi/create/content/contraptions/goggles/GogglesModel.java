package com.simibubi.create.content.contraptions.goggles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.foundation.block.render.WrappedBakedModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;

public class GogglesModel extends WrappedBakedModel {

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