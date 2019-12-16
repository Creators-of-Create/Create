package com.simibubi.create.modules.contraptions;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

public interface IWrenchable {

	public default ActionResultType onWrenched(BlockState state, ItemUseContext context) {
		return ActionResultType.PASS;
	}

}
