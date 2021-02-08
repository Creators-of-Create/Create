package com.simibubi.create.content.contraptions.components.structureMovement.gantry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntityRenderer;

import net.minecraft.client.renderer.entity.EntityRendererManager;

public class GantryContraptionEntityRenderer extends AbstractContraptionEntityRenderer<GantryContraptionEntity> {

	public GantryContraptionEntityRenderer(EntityRendererManager p_i46179_1_) {
		super(p_i46179_1_);
	}

	@Override
	protected void transform(GantryContraptionEntity contraptionEntity, float partialTicks,
		MatrixStack[] matrixStacks) {}

}
