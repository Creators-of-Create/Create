package com.simibubi.create.content.kinetics.base;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.render.VirtualRenderHelper;

public class SingleRotatingInstance<T extends KineticBlockEntity> extends KineticBlockEntityInstance<T> {

	protected RotatingInstance rotatingModel;

	public SingleRotatingInstance(VisualizationContext materialManager, T blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	public void init(float pt) {
		rotatingModel = instancerProvider.instancer(AllInstanceTypes.ROTATING, model(), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();
		setup(rotatingModel);
	}

	@Override
	public void update(float pt) {
		updateRotation(rotatingModel);
	}

	@Override
	public void updateLight() {
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
