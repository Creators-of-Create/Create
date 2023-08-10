package com.simibubi.create.content.redstone.diodes;

import com.simibubi.create.content.equipment.wrench.IWrenchable;

import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractDiodeBlock extends DiodeBlock implements IWrenchable {

	public AbstractDiodeBlock(Properties builder) {
		super(builder);
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}
}
