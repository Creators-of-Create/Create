package com.simibubi.create.content.trains.entity;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.backend.instancing.entity.EntityInstance;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.content.trains.bogey.BogeyInstance;
import com.simibubi.create.content.trains.bogey.BogeyRenderer;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Iterate;

public class CarriageContraptionInstance extends EntityInstance<CarriageContraptionEntity> implements DynamicInstance {

	private final PoseStack ms = new PoseStack();

	private Carriage carriage;
	private Couple<BogeyInstance> bogeys;
	private Couple<Boolean> bogeyHidden;

	public CarriageContraptionInstance(MaterialManager materialManager, CarriageContraptionEntity entity) {
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
	public void beginFrame() {
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

		Vector3f instancePosition = getInstancePosition(partialTicks);
		TransformStack.cast(ms)
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
				instance.updateLight(world, entity);
		});
	}

	@Override
	public void remove() {
		if (bogeys == null)
			return;

		bogeys.forEach(instance -> {
			if (instance != null) {
				instance.commonRenderer.ifPresent(BogeyRenderer::remove);
				instance.renderer.remove();
			}
		});
	}

	@Override
	public boolean decreaseFramerateWithDistance() {
		return false;
	}
}
