package com.simibubi.create.content.kinetics.crank;

import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visual.DynamicVisual;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.instance.TransformedInstance;
import com.simibubi.create.content.kinetics.base.SingleRotatingInstance;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HandCrankInstance extends SingleRotatingInstance<HandCrankBlockEntity> implements DynamicVisual {

	private TransformedInstance crank;
	private Direction facing;

	public HandCrankInstance(VisualizationContext modelManager, HandCrankBlockEntity blockEntity) {
		super(modelManager, blockEntity);
		facing = blockState.getValue(BlockStateProperties.FACING);
		Model model = blockEntity.getRenderedHandleInstance();
		crank = model.createInstance();
		rotateCrank();
	}

	@Override
	public void beginFrame() {
		if (crank == null)
			return;

		rotateCrank();
	}

	private void rotateCrank() {
		Direction.Axis axis = facing.getAxis();
		float angle = blockEntity.getIndependentAngle(AnimationTickHolder.getPartialTicks());

		crank.loadIdentity()
			.translate(getVisualPosition())
			.center()
			.rotate(Direction.get(Direction.AxisDirection.POSITIVE, axis), angle)
			.uncenter();
	}

	@Override
	public void init() {
		if (blockEntity.shouldRenderShaft())
			super.init();
	}

	@Override
	protected void _delete() {
		if (blockEntity.shouldRenderShaft())
			super._delete();
		if (crank != null)
			crank.delete();
	}

	@Override
	public void update() {
		if (blockEntity.shouldRenderShaft())
			super.update();
	}

	@Override
	public void updateLight() {
		if (blockEntity.shouldRenderShaft())
			super.updateLight();
		if (crank != null)
			relight(pos, crank);
	}
}
