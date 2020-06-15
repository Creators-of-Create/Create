package com.simibubi.create.content.contraptions.fluids;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

public class FluidTankCTBehaviour extends HorizontalCTBehaviour {

	public FluidTankCTBehaviour(CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift) {
		super(layerShift, topShift);
	}
	
	@Override
	public boolean buildContextForOccludedDirections() {
		return true;
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState other, ILightReader reader, BlockPos pos, BlockPos otherPos,
		Direction face) {
		// TODO only if TEs are actually connected
		return state.getBlock() == other.getBlock();
	}
}
