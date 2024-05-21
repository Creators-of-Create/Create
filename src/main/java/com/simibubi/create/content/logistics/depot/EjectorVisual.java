package com.simibubi.create.content.logistics.depot;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.model.Models;
import com.jozufozu.flywheel.lib.visual.SimpleDynamicVisual;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftVisual;

public class EjectorVisual extends ShaftVisual<EjectorBlockEntity> implements SimpleDynamicVisual {

	protected final TransformedInstance plate;

	private float lastProgress = Float.NaN;

	public EjectorVisual(VisualizationContext dispatcher, EjectorBlockEntity blockEntity) {
		super(dispatcher, blockEntity);

		plate = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.EJECTOR_TOP)).createInstance();
	}

	@Override
	public void init(float pt) {
		pivotPlate(getLidProgress(pt));

		super.init(pt);
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		float lidProgress = getLidProgress(ctx.partialTick());

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

	private float getLidProgress(float pt) {
		return blockEntity.getLidProgress(pt);
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
