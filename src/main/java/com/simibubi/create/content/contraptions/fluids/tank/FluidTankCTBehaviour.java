package com.simibubi.create.content.contraptions.fluids.tank;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;

public class FluidTankCTBehaviour extends HorizontalCTBehaviour {

	public FluidTankCTBehaviour(CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift) {
		super(layerShift, topShift);
	}

	public boolean buildContextForOccludedDirections() {
		return true;
	}

	@Override
	public boolean connectsTo(BlockState state, BlockState other, IBlockDisplayReader reader, BlockPos pos, BlockPos otherPos,
		Direction face) {
		return state.getBlock() == other.getBlock() && FluidTankConnectivityHandler.isConnected(reader, pos, otherPos);
	}
}
