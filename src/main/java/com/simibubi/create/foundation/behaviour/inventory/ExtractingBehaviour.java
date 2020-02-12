package com.simibubi.create.foundation.behaviour.inventory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.tuple.Pair;

import com.simibubi.create.config.AllConfigs;
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

	private Function<ItemStack, Integer> customAmountFilter;
	private Predicate<ItemStack> customFilter;
	private Consumer<ItemStack> callback;

	public ExtractingBehaviour(SmartTileEntity te, Supplier<List<Pair<BlockPos, Direction>>> attachments,
			Consumer<ItemStack> onExtract) {
		super(te, attachments);
		customAmountFilter = stack -> 64;
		customFilter = stack -> true;
		setCallback(onExtract);
	}

	public ExtractingBehaviour withAmountThreshold(Function<ItemStack, Integer> filter) {
		this.customAmountFilter = filter;
		return this;
	}

	public ExtractingBehaviour withAdditionalFilter(Predicate<ItemStack> filter) {
		this.customFilter = filter;
		return this;
	}

	public boolean extract() {
		return extract(getAmountToExtract());
	}

	public int getAmountToExtract() {
		int amount = -1;
		FilteringBehaviour filter = get(tileEntity, FilteringBehaviour.TYPE);
		if (filter != null && !filter.anyAmount())
			amount = filter.getAmount();
		return amount;
	}

	public boolean extract(int exactAmount) {
		if (getWorld().isRemote)
			return false;
		if (AllConfigs.SERVER.control.freezeExtractors.get())
			return false;

		Predicate<ItemStack> test = getFilterTest();
		for (IItemHandler inv : getInventories()) {
			ItemStack extract = ItemStack.EMPTY;
			if (exactAmount != -1)
				extract = ItemHelper.extract(inv, test, exactAmount, false);
			else
				extract = ItemHelper.extract(inv, test, customAmountFilter, false);

			if (!extract.isEmpty()) {
				callback.accept(extract);
				return true;
			}
		}

		return false;
	}

	public Predicate<ItemStack> getFilterTest() {
		Predicate<ItemStack> test = customFilter;
		FilteringBehaviour filter = get(tileEntity, FilteringBehaviour.TYPE);
		if (filter != null)
			test = customFilter.and(filter::test);
		return test;
	}

	@Override
	public IBehaviourType<?> getType() {
		return TYPE;
	}

	public void setCallback(Consumer<ItemStack> callback) {
		this.callback = callback;
	}

}
