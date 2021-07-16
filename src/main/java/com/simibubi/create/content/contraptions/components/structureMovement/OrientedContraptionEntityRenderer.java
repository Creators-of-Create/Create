package com.simibubi.create.content.contraptions.components.structureMovement;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;

public class OrientedContraptionEntityRenderer extends ContraptionEntityRenderer<OrientedContraptionEntity> {

	public OrientedContraptionEntityRenderer(EntityRendererManager p_i46179_1_) {
		super(p_i46179_1_);
	}

	@Override
	public boolean shouldRender(OrientedContraptionEntity entity, ClippingHelper pCamera, double pCamX,
		double pCamY, double pCamZ) {
		if (!super.shouldRender(entity, pCamera, pCamX, pCamY, pCamZ))
			return false;
		if (entity.getContraption()
				  .getType() == ContraptionType.MOUNTED && entity.getVehicle() == null)
			return false;
		return true;
	}

}
