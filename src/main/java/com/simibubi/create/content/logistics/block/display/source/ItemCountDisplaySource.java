package com.simibubi.create.content.logistics.block.display.source;

import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import com.simibubi.create.content.logistics.block.redstone.ContentObserverTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.utility.Components;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;

public class ItemCountDisplaySource extends NumericSingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		BlockEntity sourceTE = context.getSourceTE();
		if (!(sourceTE instanceof ContentObserverTileEntity cote))
			return ZERO.copy();

		InvManipulationBehaviour invManipulationBehaviour = cote.getBehaviour(InvManipulationBehaviour.TYPE);
		FilteringBehaviour filteringBehaviour = cote.getBehaviour(FilteringBehaviour.TYPE);
		IItemHandler handler = invManipulationBehaviour.getInventory();

		if (handler == null)
			return ZERO.copy();

		int collected = 0;
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack stack = handler.extractItem(i, handler.getSlotLimit(i), true);
			if (stack.isEmpty())
				continue;
			if (!filteringBehaviour.test(stack))
				continue;
			collected += stack.getCount();
		}

		return Components.literal(String.valueOf(collected));
	}

	@Override
	protected String getTranslationKey() {
		return "count_items";
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}

}
