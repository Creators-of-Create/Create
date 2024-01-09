package com.simibubi.create.content.kinetics.base;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.instance.Instancer;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.model.Models;
import com.simibubi.create.foundation.render.AllInstanceTypes;

import net.minecraft.world.level.block.state.BlockState;

public class SingleRotatingInstance<T extends KineticBlockEntity> extends KineticBlockEntityInstance<T> {

	protected RotatingInstance rotatingModel;

	public SingleRotatingInstance(VisualizationContext materialManager, T blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	public void init(float pt) {
		var instance = getModel().createInstance();
		rotatingModel = setup(instance);
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

	protected BlockState getRenderedBlockState() {
		return blockState;
	}

	protected Instancer<RotatingInstance> getModel() {
		return instancerProvider.instancer(AllInstanceTypes.ROTATING, model(), RenderStage.AFTER_BLOCK_ENTITIES);
	}

	protected Model model() {
		return Models.block(getRenderedBlockState());
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(rotatingModel);
	}
}
