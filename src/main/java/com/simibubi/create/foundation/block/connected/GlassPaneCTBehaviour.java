package com.simibubi.create.foundation.block.connected;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

public class GlassPaneCTBehaviour extends StandardCTBehaviour {

	public GlassPaneCTBehaviour(CTSpriteShiftEntry shift) {
		super(shift);
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState other, ILightReader reader, BlockPos pos, BlockPos otherPos,
		Direction face) {
		return state.getBlock() == other.getBlock();
	}

	@Override
	protected boolean reverseUVsHorizontally(BlockState state, net.minecraft.util.Direction face) {
		if (face.getAxisDirection() == AxisDirection.NEGATIVE)
			return true;
		return super.reverseUVsHorizontally(state, face);
	}
}
