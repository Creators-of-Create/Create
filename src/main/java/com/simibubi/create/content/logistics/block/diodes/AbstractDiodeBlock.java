package com.simibubi.create.content.logistics.block.diodes;

import com.simibubi.create.content.contraptions.wrench.IWrenchable;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.DiodeBlock;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public abstract class AbstractDiodeBlock extends DiodeBlock implements IWrenchable {

	public AbstractDiodeBlock(Properties builder) {
		super(builder);
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}
}
