package com.simibubi.create.content.kinetics.base;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.render.VirtualRenderHelper;

public class SingleRotatingVisual<T extends KineticBlockEntity> extends KineticBlockEntityVisual<T> {

	protected RotatingInstance rotatingModel;

	public SingleRotatingVisual(VisualizationContext context, T blockEntity) {
		super(context, blockEntity);
	}

	@Override
	public void init(float pt) {
		rotatingModel = instancerProvider.instancer(AllInstanceTypes.ROTATING, model(), RenderStage.AFTER_BLOCK_ENTITIES)
				.createInstance();
		setup(rotatingModel);
		super.init(pt);
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
