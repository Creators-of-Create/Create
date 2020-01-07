package com.simibubi.create.foundation.behaviour.inventory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.foundation.behaviour.base.IBehaviourType;
import com.simibubi.create.foundation.behaviour.base.SmartTileEntity;
import com.simibubi.create.foundation.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;

public class ExtractingBehaviour extends InventoryManagementBehaviour {

	public static IBehaviourType<ExtractingBehaviour> TYPE = new IBehaviourType<ExtractingBehaviour>() {
	};

	private Predicate<ItemStack> extractionFilter;
	private Consumer<ItemStack> callback;

	public ExtractingBehaviour(SmartTileEntity te, Supplier<List<Pair<BlockPos, Direction>>> attachments,
			Consumer<ItemStack> onExtract) {
		super(te, attachments);
		extractionFilter = stack -> true;
		callback = onExtract;
	}

	public ExtractingBehaviour withSpecialFilter(Predicate<ItemStack> filter) {
		this.extractionFilter = filter;
		return this;
	}

	public boolean extract() {
		if (getWorld().isRemote)
			return false;

		int amount = -1;
		Predicate<ItemStack> test = extractionFilter;

		FilteringBehaviour filter = get(tileEntity, FilteringBehaviour.TYPE);
		if (filter != null) {
			ItemStack filterItem = filter.getFilter();
			amount = filterItem.isEmpty() ? -1 : filterItem.getCount();
			test = extractionFilter.and(filter::test);
		}

		for (IItemHandler inv : getInventories()) {
			ItemStack extract = ItemHelper.extract(inv, test, amount, false);
			if (!extract.isEmpty()) {
				callback.accept(extract);
				return true;
			}
		}
		
		return false;
	}

	@Override
	public IBehaviourType<?> getType() {
		return TYPE;
	}

}
