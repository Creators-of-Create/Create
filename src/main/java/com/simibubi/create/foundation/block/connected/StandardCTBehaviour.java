package com.simibubi.create.foundation.block.connected;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

public class StandardCTBehaviour extends ConnectedTextureBehaviour {

	CTSpriteShiftEntry shift;

	public StandardCTBehaviour(CTSpriteShiftEntry shift) {
		this.shift = shift;
	}

	@Override
	public CTSpriteShiftEntry get(BlockState state, Direction direction) {
		return shift;
	}

}
