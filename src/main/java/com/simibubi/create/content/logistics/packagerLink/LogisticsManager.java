package com.simibubi.create.content.logistics.packagerLink;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.simibubi.create.api.packager.InventoryIdentifier;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagingRequest;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.foundation.utility.TickBasedCache;

import net.createmod.catnip.data.Pair;
import net.minecraft.world.item.ItemStack;

public class LogisticsManager {

	private static Random r = new Random();

	public static final Cache<UUID, InventorySummary> ACCURATE_SUMMARIES = new TickBasedCache<>(1, false);
	public static final Cache<UUID, InventorySummary> SUMMARIES = new TickBasedCache<>(20, false);

	public static InventorySummary getSummaryOfNetwork(UUID freqId, boolean accurate) {
		try {
			Cache<UUID, InventorySummary> cacheToUse = accurate ? LogisticsManager.ACCURATE_SUMMARIES
				: LogisticsManager.SUMMARIES;
			return cacheToUse.get(freqId, () -> createSummaryOfNetwork(freqId));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return InventorySummary.EMPTY;
	}

	private static InventorySummary createSummaryOfNetwork(UUID freqId) {
		InventorySummary summaryOfLinks = new InventorySummary();
		Set<InventoryIdentifier> processedInventories = new HashSet<>();
		for (LogisticallyLinkedBehaviour link : LogisticallyLinkedBehaviour.getAllPresent(freqId, false)) {

			// Skip inventories already presented by other links
			InventoryIdentifier currentInventoryId = getInventoryIdentifierFromLink(link);
			if (currentInventoryId != null && !processedInventories.add(currentInventoryId))
				continue;

			InventorySummary summary = link.getSummary(null);
			if (summary != InventorySummary.EMPTY) {
				summaryOfLinks.contributingLinks++;
				summaryOfLinks.add(summary);
			}
		}

		return summaryOfLinks;
	}

	public static int getStockOf(UUID freqId, ItemStack stack, @Nullable IdentifiedInventory ignoredHandler) {
		int sum = 0;
		for (LogisticallyLinkedBehaviour link : LogisticallyLinkedBehaviour.getAllPresent(freqId, false))
			sum += link.getSummary(ignoredHandler)
				.getCountOf(stack);
		return sum;
	}

	public static boolean broadcastPackageRequest(UUID freqId, RequestType type, PackageOrderWithCrafts order,
												  @Nullable IdentifiedInventory ignoredHandler, String address) {
		if (order.isEmpty())
			return false;

		Multimap<PackagerBlockEntity, PackagingRequest> requests = findPackagersForRequest(freqId, order,
			ignoredHandler, address);

		// Check if packagers have accumulated too many packages already
		for (PackagerBlockEntity packager : requests.keySet())
			if (packager.isTooBusyFor(type))
				return false;

		// Actually perform package creation
		performPackageRequests(requests);
		return true;
	}
    
    public static Multimap<PackagerBlockEntity, PackagingRequest> findPackagersForRequest(UUID freqId,
                                                                                          PackageOrderWithCrafts order, @Nullable IdentifiedInventory ignoredHandler, String address) {
        // Aggregate identical items to prevent same-tick stale cache issues (over-requesting)
        List<BigItemStack> aggregatedStacks = new ArrayList<>();

        for (BigItemStack originalStack : order.stacks()) {
            if (originalStack.stack.isEmpty() || originalStack.count <= 0) 
                continue;

            boolean merged = false;
            for (BigItemStack existing : aggregatedStacks) {
                if (ItemStack.isSameItemSameComponents(existing.stack, originalStack.stack)) {
                    existing.count += originalStack.count;
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                // Instantiate new BigItemStack to preserve the original order specifics for defrag
                aggregatedStacks.add(new BigItemStack(originalStack.stack, originalStack.count));
            }
        }

        Multimap<PackagerBlockEntity, PackagingRequest> requests = HashMultimap.create();

        // Packages need to track their index and successors for successful defrag
        Iterable<LogisticallyLinkedBehaviour> allAvailableLinks = LogisticallyLinkedBehaviour.getAllPresent(freqId, true);

        // Group links by InventoryIdentifier and randomly select one from each group
        Map<InventoryIdentifier, List<LogisticallyLinkedBehaviour>> linksByInventory = new HashMap<>();
        List<LogisticallyLinkedBehaviour> availableLinks = new ArrayList<>();

        for (LogisticallyLinkedBehaviour link : allAvailableLinks) {
            InventoryIdentifier inventoryId = getInventoryIdentifierFromLink(link);
            if (inventoryId != null) {
                linksByInventory.computeIfAbsent(inventoryId, k -> new ArrayList<>()).add(link);
            } else {
                availableLinks.add(link);
            }
        }

        for (List<LogisticallyLinkedBehaviour> linkGroup : linksByInventory.values()) {
            if (!linkGroup.isEmpty()) {
                LogisticallyLinkedBehaviour selectedLink = linkGroup.get(r.nextInt(linkGroup.size()));
                availableLinks.add(selectedLink);
            }
        }

        List<LogisticallyLinkedBehaviour> usedLinks = new ArrayList<>();
        MutableBoolean finalLinkTracker = new MutableBoolean(false);

        // First box needs to carry the order specifics for successful defrag
        PackageOrderWithCrafts context = order;
        int orderId = r.nextInt();

        // Iterate over aggregated requirements instead of individual order slots
        for (int i = 0; i < aggregatedStacks.size(); i++) {
            BigItemStack entry = aggregatedStacks.get(i);
            int remainingCount = entry.count;
            boolean finalEntry = i == aggregatedStacks.size() - 1;
            ItemStack requestedItem = entry.stack;

            for (LogisticallyLinkedBehaviour link : availableLinks) {
                int usedIndex = usedLinks.indexOf(link);
                int linkIndex = usedIndex == -1 ? usedLinks.size() : usedIndex;
                MutableBoolean isFinalLink = new MutableBoolean(false);
                if (linkIndex == usedLinks.size() - 1)
                    isFinalLink = finalLinkTracker;

                Pair<PackagerBlockEntity, PackagingRequest> request = link.processRequest(requestedItem, remainingCount,
                        address, linkIndex, isFinalLink, orderId, context, ignoredHandler);
                
                if (request == null)
                    continue;

                requests.put(request.getFirst(), request.getSecond());

                int processedCount = request.getSecond().getCount();
                
                if (processedCount > 0) {
                    // Only the first package needs to carry the order context for successful defrag
                    context = null; 

                    if (usedIndex == -1) {
                        usedLinks.add(link);
                        finalLinkTracker = isFinalLink;
                    }
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

	@Nullable
	private static InventoryIdentifier getInventoryIdentifierFromLink(LogisticallyLinkedBehaviour link) {

		if (!(link.blockEntity instanceof PackagerLinkBlockEntity plbe)) {
			return null;
		}

		PackagerBlockEntity packager = plbe.getPackager();
		if (packager == null || !packager.targetInventory.hasInventory()) {
			return null;
		}

		IdentifiedInventory identifiedInventory = packager.targetInventory.getIdentifiedInventory();
		InventoryIdentifier result = identifiedInventory != null ? identifiedInventory.identifier() : null;
		return result;
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