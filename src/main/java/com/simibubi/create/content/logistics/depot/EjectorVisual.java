package com.simibubi.create.content.logistics.depot;

import java.util.function.Consumer;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.ShaftVisual;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;

public class EjectorVisual extends ShaftVisual<EjectorBlockEntity> implements SimpleDynamicVisual {

	protected final TransformedInstance plate;

	private float lastProgress = Float.NaN;

	public EjectorVisual(VisualizationContext dispatcher, EjectorBlockEntity blockEntity, float partialTick) {
		super(dispatcher, blockEntity, partialTick);

		plate = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.EJECTOR_TOP)).createInstance();

		pivotPlate(getLidProgress(partialTick));
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
	public void updateLight(float partialTick) {
		super.updateLight(partialTick);
		relight(plate);
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
