package com.simibubi.create.content.contraptions.render;

import com.simibubi.create.content.contraptions.ContraptionType;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class OrientedContraptionEntityRenderer extends ContraptionEntityRenderer<OrientedContraptionEntity> {

	public OrientedContraptionEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(OrientedContraptionEntity entity, Frustum p_225626_2_, double p_225626_3_,
		double p_225626_5_, double p_225626_7_) {
		if (!super.shouldRender(entity, p_225626_2_, p_225626_3_, p_225626_5_, p_225626_7_))
			return false;
		if (entity.getContraption()
				  .getType() == ContraptionType.MOUNTED && entity.getVehicle() == null)
			return false;
		return true;
	}

}
