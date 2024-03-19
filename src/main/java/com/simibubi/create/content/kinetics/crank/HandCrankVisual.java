package com.simibubi.create.content.kinetics.crank;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visual.VisualFrameContext;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.InstanceTypes;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.jozufozu.flywheel.lib.visual.SimpleDynamicVisual;
import com.simibubi.create.content.kinetics.base.SingleRotatingVisual;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HandCrankVisual extends SingleRotatingVisual<HandCrankBlockEntity> implements SimpleDynamicVisual {

	private TransformedInstance crank;
	private Direction facing;

	public HandCrankVisual(VisualizationContext modelManager, HandCrankBlockEntity blockEntity) {
		super(modelManager, blockEntity);
		facing = blockState.getValue(BlockStateProperties.FACING);
		Model model = blockEntity.getRenderedHandleInstance();
		crank = instancerProvider.instancer(InstanceTypes.TRANSFORMED, model)
				.createInstance();
	}

	@Override
	public void beginFrame(VisualFrameContext ctx) {
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
	public void init(float pt) {
		rotateCrank(pt);

		// FIXME: need to call super.super.init here
		if (blockEntity.shouldRenderShaft())
			super.init(pt);

		updateLight();
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
	public void updateLight() {
		if (blockEntity.shouldRenderShaft())
			super.updateLight();
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
