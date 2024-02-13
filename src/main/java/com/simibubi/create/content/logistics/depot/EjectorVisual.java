package com.simibubi.create.content.logistics.depot;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftVisual;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

public class EjectorVisual extends ShaftVisual<EjectorBlockEntity> implements DynamicVisual {

	protected final TransformedInstance plate;

	private float lastProgress = Float.NaN;

	public EjectorVisual(VisualizationContext dispatcher, EjectorBlockEntity blockEntity) {
		super(dispatcher, blockEntity);

		plate = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.EJECTOR_TOP)).createInstance();

		pivotPlate();
	}

	@Override
	public void beginFrame(VisualFrameContext ctx) {
		float lidProgress = getLidProgress();

		if (lidProgress == lastProgress) {
			return;
		}

		pivotPlate(lidProgress);
		lastProgress = lidProgress;
	}

	@Override
	public void updateLight() {
		super.updateLight();
		relight(pos, plate);
	}

	@Override
    protected void _delete() {
		super._delete();
		plate.delete();
	}

	private void pivotPlate() {
		pivotPlate(getLidProgress());
	}

	private float getLidProgress() {
		return blockEntity.getLidProgress(AnimationTickHolder.getPartialTicks());
	}

	private void pivotPlate(float lidProgress) {
		float angle = lidProgress * 70;

		EjectorRenderer.applyLidAngle(blockEntity, angle, plate.loadIdentity().translate(getVisualPosition()));
		plate.setChanged();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		super.collectCrumblingInstances(consumer);
		consumer.accept(plate);
	}
}
