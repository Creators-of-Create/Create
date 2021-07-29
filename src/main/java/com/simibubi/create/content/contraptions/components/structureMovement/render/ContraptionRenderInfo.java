package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.util.math.MathHelper;

public class ContraptionRenderInfo {
	public final Contraption contraption;
	public final PlacementSimulationWorld renderWorld;

	private ContraptionMatrices matrices = ContraptionMatrices.IDENTITY;
	private boolean visible;

	public ContraptionRenderInfo(Contraption contraption, PlacementSimulationWorld renderWorld) {
		this.contraption = contraption;
		this.renderWorld = renderWorld;
	}

    public int getEntityId() {
        return contraption.entity.getId();
    }

    public boolean isDead() {
        return !contraption.entity.isAlive();
    }

    public void beginFrame(ClippingHelper clippingHelper, MatrixStack mainStack, double camX, double camY, double camZ) {
		AbstractContraptionEntity entity = contraption.entity;

		visible = clippingHelper.isVisible(entity.getBoundingBoxForCulling().inflate(2));

		mainStack.pushPose();

		double x = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), entity.xOld, entity.getX()) - camX;
		double y = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), entity.yOld, entity.getY()) - camY;
		double z = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), entity.zOld, entity.getZ()) - camZ;

		mainStack.translate(x, y, z);

		matrices = new ContraptionMatrices(mainStack, entity);

		mainStack.popPose();
	}

	public boolean isVisible() {
		return visible && contraption.entity.isAlive();
	}

	public ContraptionMatrices getMatrices() {
		return matrices;
	}
}
