package com.simibubi.create.content.trains.entity;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.render.ContraptionVisual;
import com.simibubi.create.content.trains.bogey.BogeyVisual;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;

import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.transform.TransformStack;

public class CarriageContraptionVisual extends ContraptionVisual<CarriageContraptionEntity> {
	private final PoseStack ms = new PoseStack();

	@Nullable
	private Carriage carriage;
	@Nullable
	private Couple<@Nullable VisualizedBogey> bogeys;
	private Couple<Boolean> bogeyHidden = Couple.create(() -> false);

	public CarriageContraptionVisual(VisualizationContext context, CarriageContraptionEntity entity, float partialTick) {
		super(context, entity, partialTick);
		entity.bindInstance(this);
	}

	@Override
	protected void init(float pt) {
		carriage = entity.getCarriage();

		if (carriage != null) {
			bogeys = carriage.bogeys.mapNotNull(bogey -> VisualizedBogey.of(visualizationContext, bogey, pt));
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

		Vector3f visualPosition = getVisualPosition(partialTick);
		TransformStack.of(ms)
			.translate(visualPosition);

		for (boolean current : Iterate.trueAndFalse) {
			VisualizedBogey visualizedBogey = bogeys.get(current);
			if (visualizedBogey == null)
				continue;

			if (bogeyHidden.get(current)) {
				visualizedBogey.visual.hide();
				continue;
			}

			ms.pushPose();
			CarriageBogey bogey = visualizedBogey.bogey;

			CarriageContraptionEntityRenderer.translateBogey(ms, bogey, bogeySpacing, viewYRot, viewXRot, partialTick);
			ms.translate(0, -1.5 - 1 / 128f, 0);

			visualizedBogey.visual.update(bogey.wheelAngle.getValue(partialTick), ms);
			ms.popPose();
		}

		ms.popPose();
	}

	@Override
	public void updateLight(float partialTick) {
		super.updateLight(partialTick);

		if (bogeys == null)
			return;

		bogeys.forEach(bogey -> {
			if (bogey != null) {
				int packedLight = CarriageContraptionEntityRenderer.getBogeyLightCoords(entity, bogey.bogey, partialTick);
				bogey.visual.updateLight(packedLight);
			}
		});
	}

	@Override
	public void _delete() {
		super._delete();

		if (bogeys == null)
			return;

		bogeys.forEach(bogey -> {
			if (bogey != null) {
				bogey.visual.delete();
			}
		});
	}

	private record VisualizedBogey(CarriageBogey bogey, BogeyVisual visual) {
		@Nullable
		static VisualizedBogey of(VisualizationContext ctx, CarriageBogey bogey, float partialTick) {
			BogeyVisual visual = bogey.getStyle().createVisual(ctx, bogey, partialTick);
			if (visual == null) {
				return null;
			}
			return new VisualizedBogey(bogey, visual);
		}
	}
}
