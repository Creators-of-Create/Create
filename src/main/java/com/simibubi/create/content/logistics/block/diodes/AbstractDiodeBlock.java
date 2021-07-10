package com.simibubi.create.content.logistics.block.diodes;

import com.simibubi.create.content.contraptions.wrench.IWrenchable;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneDiodeBlock;

public abstract class AbstractDiodeBlock extends RedstoneDiodeBlock implements IWrenchable {

	public AbstractDiodeBlock(Properties builder) {
		super(builder);
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return true;
	}
}
