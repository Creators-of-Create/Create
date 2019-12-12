package com.simibubi.create.modules.contraptions.relays.elementary;

import com.simibubi.create.foundation.block.IWithoutBlockItem;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;

import net.minecraft.block.Blocks;

public class ShaftHalfBlock extends ProperDirectionalBlock implements IWithoutBlockItem {

	public ShaftHalfBlock() {
		super(Properties.from(Blocks.AIR));
	}

}
