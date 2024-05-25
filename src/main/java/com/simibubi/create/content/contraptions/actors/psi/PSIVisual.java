package com.simibubi.create.content.contraptions.actors.psi;

import java.util.function.Consumer;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visual.TickableVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.visual.AbstractBlockEntityVisual;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import dev.engine_room.flywheel.lib.visual.SimpleTickableVisual;

public class PSIVisual extends AbstractBlockEntityVisual<PortableStorageInterfaceBlockEntity> implements SimpleDynamicVisual, SimpleTickableVisual {

	private final PIInstance instance;

	public PSIVisual(VisualizationContext visualizationContext, PortableStorageInterfaceBlockEntity blockEntity) {
		super(visualizationContext, blockEntity);

		instance = new PIInstance(visualizationContext.instancerProvider(), blockState, getVisualPosition());
	}

	@Override
	public void init(float pt) {
		instance.init(isLit());
		super.init(pt);
	}

	@Override
	public void tick(TickableVisual.Context ctx) {
		instance.tick(isLit());
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		instance.beginFrame(blockEntity.getExtensionDistance(ctx.partialTick()));
	}

	@Override
	public void updateLight() {
		relight(pos, instance.middle, instance.top);
	}

	@Override
	protected void _delete() {
		instance.remove();
	}

	private boolean isLit() {
		return blockEntity.isConnected();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		instance.collectCrumblingInstances(consumer);
	}
}
