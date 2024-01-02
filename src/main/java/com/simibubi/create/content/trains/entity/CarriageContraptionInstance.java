package com.simibubi.create.content.trains.entity;

import org.joml.Vector3f;

import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.transform.TransformStack;
import com.jozufozu.flywheel.lib.visual.AbstractEntityVisual;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.trains.bogey.BogeyInstance;
import com.simibubi.create.content.trains.bogey.BogeyRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;

public class CarriageContraptionInstance extends AbstractEntityVisual<CarriageContraptionEntity> implements DynamicVisual {

	private final PoseStack ms = new PoseStack();

	private Carriage carriage;
	private Couple<BogeyInstance> bogeys;
	private Couple<Boolean> bogeyHidden;

	public CarriageContraptionInstance(VisualizationContext materialManager, CarriageContraptionEntity entity) {
		super(materialManager, entity);
		bogeyHidden = Couple.create(() -> false);
		entity.bindInstance(this);
	}

	@Override
	public void init() {
		carriage = entity.getCarriage();

		if (carriage == null)
			return;

		bogeys = carriage.bogeys.mapNotNullWithParam((bogey, manager) ->
				bogey.getStyle().createInstance(bogey, bogey.type.getSize(), manager), materialManager);
		updateLight();
	}

	public void setBogeyVisibility(boolean first, boolean visible) {
		bogeyHidden.set(first, !visible);
	}

	@Override
	public void beginFrame(VisualFrameContext ctx) {
		if (bogeys == null) {
			if (entity.isReadyForRender())
				init();
			return;
		}

		float partialTicks = AnimationTickHolder.getPartialTicks();

		float viewYRot = entity.getViewYRot(partialTicks);
		float viewXRot = entity.getViewXRot(partialTicks);
		int bogeySpacing = carriage.bogeySpacing;

		ms.pushPose();

		Vector3f instancePosition = getVisualPosition(partialTicks);
		TransformStack.of(ms)
			.translate(instancePosition);

		for (boolean current : Iterate.trueAndFalse) {
			BogeyInstance instance = bogeys.get(current);
			if (instance == null)
				continue;
			if (bogeyHidden.get(current)) {
				instance.beginFrame(0, null);
				continue;
			}

			ms.pushPose();
			CarriageBogey bogey = instance.bogey;

			CarriageContraptionEntityRenderer.translateBogey(ms, bogey, bogeySpacing, viewYRot, viewXRot, partialTicks);
			ms.translate(0, -1.5 - 1 / 128f, 0);

			instance.beginFrame(bogey.wheelAngle.getValue(partialTicks), ms);
			ms.popPose();
		}

		ms.popPose();
	}

	@Override
	public void updateLight() {
		if (bogeys == null)
			return;

		bogeys.forEach(instance -> {
			if (instance != null)
				instance.updateLight(level, entity);
		});
	}

	@Override
	public void _delete() {
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
