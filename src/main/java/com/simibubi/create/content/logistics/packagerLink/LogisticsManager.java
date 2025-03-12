package com.simibubi.create.content.logistics.packagerLink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.google.common.cache.Cache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.foundation.utility.TickBasedCache;

import net.createmod.catnip.data.Pair;
import net.minecraft.world.item.ItemStack;

public class LogisticsManager {

	private static Random r = new Random();

	public static final Cache<UUID, InventorySummary> ACCURATE_SUMMARIES = new TickBasedCache<>(1, false);
	public static final Cache<UUID, InventorySummary> SUMMARIES = new TickBasedCache<>(20, false);

	public static InventorySummary getSummaryOfNetwork(UUID freqId, boolean accurate) {
		try {
			return (accurate ? LogisticsManager.ACCURATE_SUMMARIES : LogisticsManager.SUMMARIES).get(freqId, () -> {
				InventorySummary summaryOfLinks = new InventorySummary();
				LogisticallyLinkedBehaviour.getAllPresent(freqId, false)
					.forEach(link -> {
						InventorySummary summary = link.getSummary(null);
						if (summary != InventorySummary.EMPTY)
							summaryOfLinks.contributingLinks++;
						summaryOfLinks.add(summary);
					});
				return summaryOfLinks;
			});
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return InventorySummary.EMPTY;
	}

	public static int getStockOf(UUID freqId, ItemStack stack, @Nullable IdentifiedInventory ignoredHandler) {
		int sum = 0;
		for (LogisticallyLinkedBehaviour link : LogisticallyLinkedBehaviour.getAllPresent(freqId, false))
			sum += link.getSummary(ignoredHandler)
				.getCountOf(stack);
		return sum;
	}

	public static boolean broadcastPackageRequest(UUID freqId, RequestType type, PackageOrder order,
		@Nullable IdentifiedInventory ignoredHandler, String address, @Nullable PackageOrder orderContext) {
		if (order.isEmpty())
			return false;

		Multimap<PackagerBlockEntity, PackagingRequest> requests =
			findPackagersForRequest(freqId, order, orderContext, ignoredHandler, address);

		// Check if packagers have accumulated too many packages already
		for (PackagerBlockEntity packager : requests.keySet())
			if (packager.isTooBusyFor(type))
				return false;

		// Actually perform package creation
		performPackageRequests(requests);
		return true;
	}

	public static Multimap<PackagerBlockEntity, PackagingRequest> findPackagersForRequest(UUID freqId,
		PackageOrder order, @Nullable PackageOrder customContext, @Nullable IdentifiedInventory ignoredHandler,
		String address) {
		List<BigItemStack> stacks = new ArrayList<>();
		for (BigItemStack stack : order.stacks())
			if (!stack.stack.isEmpty() && stack.count > 0)
				stacks.add(stack);

		Multimap<PackagerBlockEntity, PackagingRequest> requests = HashMultimap.create();

		// Packages need to track their index and successors for successful defrag
		Iterable<LogisticallyLinkedBehaviour> availableLinks = LogisticallyLinkedBehaviour.getAllPresent(freqId, true);
		List<LogisticallyLinkedBehaviour> usedLinks = new ArrayList<>();
		MutableBoolean finalLinkTracker = new MutableBoolean(false);

		// First box needs to carry the order specifics for successful defrag
		PackageOrder contextToSend = order;
		if (customContext != null)
			contextToSend = customContext;

		// Packages from future orders should not be merged in the packager queue
		int orderId = r.nextInt();

		for (int i = 0; i < stacks.size(); i++) {
			BigItemStack entry = stacks.get(i);
			int remainingCount = entry.count;
			boolean finalEntry = i == stacks.size() - 1;
			ItemStack requestedItem = entry.stack;

			for (LogisticallyLinkedBehaviour link : availableLinks) {
				int usedIndex = usedLinks.indexOf(link);
				int linkIndex = usedIndex == -1 ? usedLinks.size() : usedIndex;
				MutableBoolean isFinalLink = new MutableBoolean(false);
				if (linkIndex == usedLinks.size() - 1)
					isFinalLink = finalLinkTracker;

				Pair<PackagerBlockEntity, PackagingRequest> request = link.processRequest(requestedItem, remainingCount,
					address, linkIndex, isFinalLink, orderId, contextToSend, ignoredHandler);
				if (request == null)
					continue;

				requests.put(request.getFirst(), request.getSecond());

				int processedCount = request.getSecond()
					.getCount();
				if (processedCount > 0 && usedIndex == -1) {
					contextToSend = null;
					usedLinks.add(link);
					finalLinkTracker = isFinalLink;
				}

				remainingCount -= processedCount;
				if (remainingCount > 0)
					continue;
				if (finalEntry)
					finalLinkTracker.setTrue();
				break;
			}
		}
		return requests;
	}

	public static void performPackageRequests(Multimap<PackagerBlockEntity, PackagingRequest> requests) {
		Map<PackagerBlockEntity, Collection<PackagingRequest>> asMap = requests.asMap();
		for (Entry<PackagerBlockEntity, Collection<PackagingRequest>> entry : asMap.entrySet()) {
			ArrayList<PackagingRequest> queuedRequests = new ArrayList<>(entry.getValue());
			PackagerBlockEntity packager = entry.getKey();

			if (!queuedRequests.isEmpty())
				packager.flashLink();
			for (int i = 0; i < 100; i++) {
				if (queuedRequests.isEmpty())
					break;
				packager.attemptToSend(queuedRequests);
			}

			packager.triggerStockCheck();
			packager.notifyUpdate();
		}
	}

}
