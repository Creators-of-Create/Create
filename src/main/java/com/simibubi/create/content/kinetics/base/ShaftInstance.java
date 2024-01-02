package com.simibubi.create.content.kinetics.base;

import com.jozufozu.flywheel.api.visualization.VisualizationContext;

import net.minecraft.world.level.block.state.BlockState;

public class ShaftInstance<T extends KineticBlockEntity> extends SingleRotatingInstance<T> {

	public ShaftInstance(VisualizationContext materialManager, T blockEntity) {
		super(materialManager, blockEntity);
	}

	@Override
	protected BlockState getRenderedBlockState() {
		return shaft();
	}

}
