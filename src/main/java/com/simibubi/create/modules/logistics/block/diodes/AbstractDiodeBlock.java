package com.simibubi.create.modules.logistics.block.diodes;

import com.simibubi.create.modules.contraptions.IWrenchable;

import net.minecraft.block.RedstoneDiodeBlock;

public abstract class AbstractDiodeBlock extends RedstoneDiodeBlock implements IWrenchable {

	public AbstractDiodeBlock(Properties builder) {
		super(builder);
	}
	
}
