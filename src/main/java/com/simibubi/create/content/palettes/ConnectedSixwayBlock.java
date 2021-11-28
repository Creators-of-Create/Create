package com.simibubi.create.content.palettes;

import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ConnectedSixwayBlock extends PipeBlock {

	public ConnectedSixwayBlock(float p_55159_, Properties p_55160_) {
		super(p_55159_, p_55160_);
	}

	public boolean disconnectsFromState(BlockState blockingState) {
		return false;
	}

	public boolean connectsToState(BlockState adjAbove1) {
		return false;
	}
	
	

}
