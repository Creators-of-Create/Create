package com.simibubi.create.modules.contraptions.relays.elementary;

import com.simibubi.create.foundation.block.IHaveNoBlockItem;
import com.simibubi.create.foundation.block.ProperDirectionalBlock;

import net.minecraft.block.Blocks;

public class ShaftHalfBlock extends ProperDirectionalBlock implements IHaveNoBlockItem {

	public ShaftHalfBlock() {
		super(Properties.from(Blocks.AIR));
	}

}
