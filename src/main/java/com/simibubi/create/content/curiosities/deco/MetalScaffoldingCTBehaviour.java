package com.simibubi.create.content.curiosities.deco;

import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.HorizontalCTBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class MetalScaffoldingCTBehaviour extends HorizontalCTBehaviour {

	public MetalScaffoldingCTBehaviour(CTSpriteShiftEntry layerShift, CTSpriteShiftEntry topShift) {
		super(layerShift, topShift);
	}

	@Override
	public boolean buildContextForOccludedDirections() {
		return true;
	}
	
	@Override
	public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos,
		BlockPos otherPos, Direction face) {
		return super.connectsTo(state, other, reader, pos, otherPos, face)
			&& state.getValue(MetalScaffoldingBlock.BOTTOM) && other.getValue(MetalScaffoldingBlock.BOTTOM);
	}

}
