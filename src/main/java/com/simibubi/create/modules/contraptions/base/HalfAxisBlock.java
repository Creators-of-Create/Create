package com.simibubi.create.modules.contraptions.base;

import com.simibubi.create.foundation.block.IRenderUtilityBlock;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.state.StateContainer.Builder;

public class HalfAxisBlock extends DirectionalBlock implements IRenderUtilityBlock {

	public HalfAxisBlock() {
		super(Properties.create(Material.ROCK));
	}
	
	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(FACING);
		super.fillStateContainer(builder);
	}

}
