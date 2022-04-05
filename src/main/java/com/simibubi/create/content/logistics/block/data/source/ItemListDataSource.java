package com.simibubi.create.content.logistics.block.data.source;

import java.util.ArrayList;
import java.util.stream.Stream;

import com.simibubi.create.content.logistics.block.data.DataGathererContext;
import com.simibubi.create.content.logistics.block.redstone.ContentObserverTileEntity;
import com.simibubi.create.foundation.item.CountedItemStackList;
import com.simibubi.create.foundation.tileEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.inventory.InvManipulationBehaviour;
import com.simibubi.create.foundation.utility.IntAttached;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;

public class ItemListDataSource extends ValueListDataSource {

	@Override
	protected Stream<IntAttached<MutableComponent>> provideEntries(DataGathererContext context, int maxRows) {
		BlockEntity sourceTE = context.getSourceTE();
		if (!(sourceTE instanceof ContentObserverTileEntity cote))
			return new ArrayList<IntAttached<MutableComponent>>().stream();

		InvManipulationBehaviour invManipulationBehaviour = cote.getBehaviour(InvManipulationBehaviour.TYPE);
		FilteringBehaviour filteringBehaviour = cote.getBehaviour(FilteringBehaviour.TYPE);
		IItemHandler handler = invManipulationBehaviour.getInventory();

		if (handler == null)
			return new ArrayList<IntAttached<MutableComponent>>().stream();

		return new CountedItemStackList(handler, filteringBehaviour).getTopNames(maxRows);
	}

	@Override
	protected String getTranslationKey() {
		return "list_items";
	}

	@Override
	protected boolean valueFirst() {
		return true;
	}

}
