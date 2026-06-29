package com.simibubi.create.compat.computercraft.implementation.peripherals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.compat.computercraft.implementation.ComputerUtil;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;

import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;

public class StockTickerPeripheral extends SyncedPeripheral<StockTickerBlockEntity> {

	public StockTickerPeripheral(StockTickerBlockEntity blockEntity) {
		super(blockEntity);
	}

	@LuaFunction(mainThread = true)
	public final Map<Integer, Map<String, ?>> stock(Optional<Boolean> detailed) {
		Map<Integer, Map<String, ?>> result = new HashMap<>();
		int i = 0;
		for (BigItemStack entry : blockEntity.getAccurateSummary().getStacks()) {
			i++;
			Map<String, Object> details = new HashMap<>(
				detailed.isPresent() && detailed.get()
					? VanillaDetailRegistries.ITEM_STACK.getDetails(entry.stack)
					: VanillaDetailRegistries.ITEM_STACK.getBasicDetails(entry.stack));
			details.put("count", entry.count);
			result.put(i, details);
		}
		return result;
	}

	@LuaFunction(mainThread = true)
	public final Map<String, ?> getStockItemDetail(int slot) throws LuaException {
		return ComputerUtil.getItemDetail(blockEntity.getAccurateSummary(), slot);
	}

	@LuaFunction(mainThread = true)
	public final int requestFiltered(String address, IArguments filters) throws LuaException {


		List<BigItemStack> validItems = new ArrayList<>();
		int totalItemsSent = 0;
		List<BigItemStack> stock = blockEntity.getAccurateSummary().getStacks();

		for (int i = 1; i < filters.count(); i++) {
			if (!(filters.get(i) instanceof Map<?, ?> filterTable))
				throw new LuaException("Filter must be a table");

			for (Object key : filterTable.keySet())
				if (!(key instanceof String))
					throw new LuaException("Filter keys must be strings");

			@SuppressWarnings("unchecked")
			Map<String, Object> filter = (Map<String, Object>) filterTable;

			int itemsRequested = Integer.MAX_VALUE;
			if (filterTable.containsKey("_requestCount")) {
				Object requestCount = filterTable.get("_requestCount");
				filterTable.remove("_requestCount");
				if (requestCount instanceof Number) {
					itemsRequested = ((Number) requestCount).intValue();
					if (itemsRequested < 1)
						throw new LuaException("_requestCount must be a positive number or nil for no limit");
				} else
					throw new LuaException("_requestCount must be a positive number or nil for no limit");
			}

			for (BigItemStack entry : stock) {
				int foundItems = ComputerUtil.bigItemStackToLuaTableFilter(entry, filter);
				if (foundItems > 0) {
					int toTake = Math.min(foundItems, itemsRequested);
					itemsRequested -= toTake;
					totalItemsSent += toTake;
					BigItemStack requestedItem = new BigItemStack(entry.stack.copy(), toTake);
					entry.count -= toTake;
					validItems.add(requestedItem);
				}
				if (itemsRequested <= 0)
					break;
			}
		}

		PackageOrder order = new PackageOrder(validItems);

		blockEntity.broadcastPackageRequest(RequestType.RESTOCK, order, null, address);

		return totalItemsSent;
	}

	@LuaFunction(mainThread = true)
	public Map<Integer, Map<String, ?>> list() {
		return ComputerUtil.list(blockEntity.getReceivedPaymentsHandler());
	}

	@LuaFunction(mainThread = true)
	public Map<String, ?> getItemDetail(int slot) throws LuaException {
		return ComputerUtil.getItemDetail(blockEntity.getReceivedPaymentsHandler(), slot);
	}

	@NotNull
	@Override
	public String getType() {
		return "Create_StockTicker";
	}

	@Override
	public @Nullable Object getTarget() {
		return blockEntity.getReceivedPaymentsHandler();
	}

}
