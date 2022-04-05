package com.simibubi.create.content.logistics.block.data.source;

import com.simibubi.create.content.logistics.block.data.DataGathererContext;
import com.simibubi.create.content.logistics.block.data.target.DataTargetStats;
import com.simibubi.create.content.logistics.block.redstone.ContentObserverTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;

public class ItemCountDataSource extends NumericSingleLineDataSource {

	@Override
	protected MutableComponent provideLine(DataGathererContext context, DataTargetStats stats) {
		BlockEntity sourceTE = context.getSourceTE();
		if (!(sourceTE instanceof ContentObserverTileEntity cote))
			return ZERO;

		InvManipulationBehaviour invManipulationBehaviour = cote.getBehaviour(InvManipulationBehaviour.TYPE);
		FilteringBehaviour filteringBehaviour = cote.getBehaviour(FilteringBehaviour.TYPE);
		IItemHandler handler = invManipulationBehaviour.getInventory();

		if (handler == null)
			return ZERO;

		int collected = 0;
		for (int i = 0; i < handler.getSlots(); i++) {
			ItemStack stack = handler.extractItem(i, handler.getSlotLimit(i), true);
			if (stack.isEmpty())
				continue;
			if (!filteringBehaviour.test(stack))
				continue;
			collected += stack.getCount();
		}

		return new TextComponent(String.valueOf(collected));
	}

	@Override
	protected String getTranslationKey() {
		return "count_items";
	}

	@Override
	protected boolean allowsLabeling(DataGathererContext context) {
		return true;
	}

}
