package com.simibubi.create.content.kinetics.crank;

import java.util.function.Consumer;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.visual.SimpleDynamicVisual;
import com.simibubi.create.content.kinetics.base.SingleRotatingVisual;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HandCrankVisual extends SingleRotatingVisual<HandCrankBlockEntity> implements SimpleDynamicVisual {

	private final TransformedInstance crank;
	private final Direction facing;

	public HandCrankVisual(VisualizationContext modelManager, HandCrankBlockEntity blockEntity, float partialTick) {
		super(modelManager, blockEntity, partialTick);
		facing = blockState.getValue(BlockStateProperties.FACING);
		Model model = blockEntity.getRenderedHandleInstance();
		crank = instancerProvider.instancer(InstanceTypes.TRANSFORMED, model)
				.createInstance();

		rotateCrank(partialTick);

		if (blockEntity.shouldRenderShaft())
			setup(rotatingModel);

		updateLight(partialTick);
	}

	@Override
	public void beginFrame(DynamicVisual.Context ctx) {
		if (crank == null)
			return;

		rotateCrank(ctx.partialTick());
	}

	private void rotateCrank(float pt) {
		Direction.Axis axis = facing.getAxis();
		float angle = blockEntity.getIndependentAngle(pt);

		crank.loadIdentity()
			.translate(getVisualPosition())
			.center()
			.rotate(angle, Direction.get(Direction.AxisDirection.POSITIVE, axis))
			.uncenter()
			.setChanged();
	}

	@Override
	protected void _delete() {
		if (blockEntity.shouldRenderShaft())
			super._delete();
		if (crank != null)
			crank.delete();
	}

	@Override
	public void update(float pt) {
		if (blockEntity.shouldRenderShaft())
			super.update(pt);
	}

	@Override
	public void updateLight(float partialTick) {
		if (blockEntity.shouldRenderShaft())
			super.updateLight(partialTick);
		if (crank != null)
			relight(pos, crank);
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		super.collectCrumblingInstances(consumer);
		if (crank != null) {
			consumer.accept(crank);
		}
	}
}
