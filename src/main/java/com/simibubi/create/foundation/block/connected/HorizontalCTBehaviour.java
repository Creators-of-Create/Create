package com.simibubi.create.foundation.block.connected;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;

public class HorizontalCTBehaviour extends ConnectedTextureBehaviour {

	CTSpriteShiftEntry topShift;
	CTSpriteShiftEntry layerShift;

	public HorizontalCTBehaviour(CTSpriteShiftEntry layerShift) {
		this(layerShift, null);
	}

	public HorizontalCTBehaviour(CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift) {
		this.layerShift = layerShift;
		this.topShift = topShift;
	}

	@Override
	public CTSpriteShiftEntry get(BlockState state, Direction direction) {
		return direction.getAxis()
			.isHorizontal() ? layerShift : topShift;
	}

}