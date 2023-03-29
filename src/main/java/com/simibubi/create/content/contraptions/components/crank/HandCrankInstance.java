package com.simibubi.create.content.contraptions.components.crank;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.instance.DynamicInstance;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.simibubi.create.content.contraptions.base.SingleRotatingInstance;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class HandCrankInstance extends SingleRotatingInstance<HandCrankBlockEntity> implements DynamicInstance {

	private ModelData crank;
	private Direction facing;

	public HandCrankInstance(MaterialManager modelManager, HandCrankBlockEntity blockEntity) {
		super(modelManager, blockEntity);
		facing = blockState.getValue(BlockStateProperties.FACING);
		Instancer<ModelData> model = blockEntity.getRenderedHandleInstance(getTransformMaterial());
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
			.translate(getInstancePosition())
			.centre()
			.rotate(Direction.get(Direction.AxisDirection.POSITIVE, axis), angle)
			.unCentre();
	}

	@Override
	public void init() {
		if (blockEntity.shouldRenderShaft())
			super.init();
	}

	@Override
	public void remove() {
		if (blockEntity.shouldRenderShaft())
			super.remove();
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
