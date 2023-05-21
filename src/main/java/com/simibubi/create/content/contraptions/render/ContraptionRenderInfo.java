package com.simibubi.create.content.contraptions.render;

import com.jozufozu.flywheel.core.virtual.VirtualRenderWorld;
import com.jozufozu.flywheel.event.BeginFrameEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.util.Mth;

public class ContraptionRenderInfo {
	public final Contraption contraption;
	public final VirtualRenderWorld renderWorld;

	private final ContraptionMatrices matrices = new ContraptionMatrices();
	private boolean visible;

	public ContraptionRenderInfo(Contraption contraption, VirtualRenderWorld renderWorld) {
		this.contraption = contraption;
		this.renderWorld = renderWorld;
	}

	public int getEntityId() {
		return contraption.entity.getId();
	}

	public boolean isDead() {
		return !contraption.entity.isAliveOrStale();
	}

	public void beginFrame(BeginFrameEvent event) {
		matrices.clear();

		AbstractContraptionEntity entity = contraption.entity;

		visible = event.getFrustum()
			.isVisible(entity.getBoundingBoxForCulling()
				.inflate(2));
	}

	public boolean isVisible() {
		return visible && contraption.entity.isAliveOrStale() && contraption.entity.isReadyForRender();
	}

	/**
	 * Need to call this during RenderLayerEvent.
	 */
	public void setupMatrices(PoseStack viewProjection, double camX, double camY, double camZ) {
		if (!matrices.isReady()) {
			AbstractContraptionEntity entity = contraption.entity;

			viewProjection.pushPose();

			double x = Mth.lerp(AnimationTickHolder.getPartialTicks(), entity.xOld, entity.getX()) - camX;
			double y = Mth.lerp(AnimationTickHolder.getPartialTicks(), entity.yOld, entity.getY()) - camY;
			double z = Mth.lerp(AnimationTickHolder.getPartialTicks(), entity.zOld, entity.getZ()) - camZ;

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
