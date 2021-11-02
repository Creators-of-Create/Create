package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.util.math.MathHelper;

public class ContraptionRenderInfo {
	public final Contraption contraption;
	public final PlacementSimulationWorld renderWorld;

	private final ContraptionMatrices matrices = new ContraptionMatrices();
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

    public void beginFrame(BeginFrameEvent event) {
		matrices.clear();

		AbstractContraptionEntity entity = contraption.entity;

		visible = event.getClippingHelper().isVisible(entity.getBoundingBoxForCulling().inflate(2));
	}

	public boolean isVisible() {
		return visible && contraption.entity.isAlive();
	}

	/**
	 * Need to call this during RenderLayerEvent.
	 */
	public void setupMatrices(MatrixStack viewProjection, double camX, double camY, double camZ) {
		if (!matrices.isReady()) {
			AbstractContraptionEntity entity = contraption.entity;

			viewProjection.pushPose();

			double x = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), entity.xOld, entity.getX()) - camX;
			double y = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), entity.yOld, entity.getY()) - camY;
			double z = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), entity.zOld, entity.getZ()) - camZ;

			viewProjection.translate(x, y, z);

			matrices.setup(viewProjection, entity);

			viewProjection.popPose();
		}
	}

	/**
	 * If #setupMatrices is called correctly, the returned matrices will be ready
	 */
	public ContraptionMatrices getMatrices() {
		return matrices;
	}

	public void invalidate() {

	}
}
