package com.simibubi.create.content.kinetics.base;

import java.util.function.Consumer;

import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.render.VirtualRenderHelper;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;

public class SingleRotatingVisual<T extends KineticBlockEntity> extends KineticBlockEntityVisual<T> {

	protected RotatingInstance rotatingModel;

	public SingleRotatingVisual(VisualizationContext context, T blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);
	}

	public void init(float pt) {
		rotatingModel = instancerProvider.instancer(AllInstanceTypes.ROTATING, model())
				.createInstance();
		setup(rotatingModel);
	}

	@Override
	public void update(float pt) {
		updateRotation(rotatingModel);
	}

	@Override
	public void updateLight(float partialTick) {
		relight(pos, rotatingModel);
	}

	@Override
	protected void _delete() {
		rotatingModel.delete();
	}

	protected Model model() {
		return VirtualRenderHelper.blockModel(blockState);
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(rotatingModel);
	}
}
