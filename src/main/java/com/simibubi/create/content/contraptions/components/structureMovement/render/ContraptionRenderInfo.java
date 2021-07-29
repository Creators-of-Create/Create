package com.simibubi.create.content.contraptions.components.structureMovement.render;

import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.simibubi.create.content.contraptions.components.structureMovement.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.Contraption;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.worldWrappers.PlacementSimulationWorld;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class ContraptionRenderInfo {
	public final Contraption contraption;
	public final PlacementSimulationWorld renderWorld;

	private ContraptionMatrices matrices = ContraptionMatrices.EMPTY;
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
		AbstractContraptionEntity entity = contraption.entity;

		visible = event.getClippingHelper().isVisible(entity.getBoundingBoxForCulling().inflate(2));

		event.getStack().pushPose();

		Vector3d cameraPos = event.getInfo()
				.getPosition();
		double x = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), entity.xOld, entity.getX()) - cameraPos.x;
		double y = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), entity.yOld, entity.getY()) - cameraPos.y;
		double z = MathHelper.lerp(AnimationTickHolder.getPartialTicks(), entity.zOld, entity.getZ()) - cameraPos.z;

		event.getStack().translate(x, y, z);

		matrices = new ContraptionMatrices(event.getStack(), entity);

		event.getStack().popPose();
	}

	public boolean isVisible() {
		return visible && contraption.entity.isAlive();
	}

	public ContraptionMatrices getMatrices() {
		return matrices;
	}
}
