package com.simibubi.create.content.trains.entity;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.render.ContraptionVisual;
import com.simibubi.create.content.trains.bogey.BogeyRenderer;
import com.simibubi.create.content.trains.bogey.BogeyVisual;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;

import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.transform.TransformStack;

public class CarriageContraptionVisual extends ContraptionVisual<CarriageContraptionEntity> {

	private final PoseStack ms = new PoseStack();

	private Carriage carriage;
	private Couple<BogeyVisual> bogeys;
	private Couple<Boolean> bogeyHidden;

	public CarriageContraptionVisual(VisualizationContext context, CarriageContraptionEntity entity, float partialTick) {
		super(context, entity, partialTick);
		bogeyHidden = Couple.create(() -> false);
		entity.bindInstance(this);
	}

	@Override
	protected void init(float pt) {
		carriage = entity.getCarriage();

        if (carriage != null) {
            bogeys = carriage.bogeys.mapNotNullWithParam((bogey, manager) -> bogey.getStyle()
                .createVisual(bogey, bogey.type.getSize(), manager), visualizationContext);
        }

		super.init(pt);
    }

	public void setBogeyVisibility(boolean first, boolean visible) {
		bogeyHidden.set(first, !visible);
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		super.beginFrame(ctx);
		if (bogeys == null) {
			if (entity.isReadyForRender()) {
				init(ctx.partialTick());
				updateLight(ctx.partialTick());
			}
			return;
		}

		float partialTick = ctx.partialTick();

		float viewYRot = entity.getViewYRot(partialTick);
		float viewXRot = entity.getViewXRot(partialTick);
		int bogeySpacing = carriage.bogeySpacing;

		ms.pushPose();

		Vector3f instancePosition = getVisualPosition(partialTick);
		TransformStack.of(ms)
			.translate(instancePosition);

		for (boolean current : Iterate.trueAndFalse) {
			BogeyVisual instance = bogeys.get(current);
			if (instance == null)
				continue;
			if (bogeyHidden.get(current)) {
				instance.beginFrame(0, null);
				continue;
			}

			ms.pushPose();
			CarriageBogey bogey = instance.bogey;

			CarriageContraptionEntityRenderer.translateBogey(ms, bogey, bogeySpacing, viewYRot, viewXRot, partialTick);
			ms.translate(0, -1.5 - 1 / 128f, 0);

			instance.beginFrame(bogey.wheelAngle.getValue(partialTick), ms);
			ms.popPose();
		}

		ms.popPose();
	}

	@Override
	public void updateLight(float partialTick) {
		super.updateLight(partialTick);

		if (bogeys == null)
			return;

		bogeys.forEach(instance -> {
			if (instance != null)
				instance.updateLight(level, entity);
		});
	}

	@Override
	public void _delete() {
		super._delete();

		if (bogeys == null)
			return;

		bogeys.forEach(instance -> {
			if (instance != null) {
				instance.commonRenderer.ifPresent(BogeyRenderer::remove);
				instance.renderer.remove();
			}
		});
	}
}
