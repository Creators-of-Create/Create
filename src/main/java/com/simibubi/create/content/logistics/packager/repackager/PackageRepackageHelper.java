package com.simibubi.create.content.logistics.packager.repackager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageItem.PackageOrderData;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts.CraftingEntry;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class PackageRepackageHelper {

	protected Map<Integer, List<ItemStack>> collectedPackages = new HashMap<>();

	public void clear() {
		collectedPackages.clear();
	}

	public boolean isFragmented(ItemStack box) {
		return box.has(AllDataComponents.PACKAGE_ORDER_DATA);
	}

	public int addPackageFragment(ItemStack box) {
		int collectedOrderId = PackageItem.getOrderId(box);
		if (collectedOrderId == -1)
			return -1;

		List<ItemStack> collectedOrder = collectedPackages.computeIfAbsent(collectedOrderId, $ -> Lists.newArrayList());
		collectedOrder.add(box);

		if (!isOrderComplete(collectedOrderId))
			return -1;

		return collectedOrderId;
	}

	public List<BigItemStack> repack(int orderId, RandomSource r) {
		List<BigItemStack> exportingPackages = new ArrayList<>();
		String address = "";
		PackageOrderWithCrafts orderContext = null;
		InventorySummary summary = new InventorySummary();

		for (ItemStack box : collectedPackages.get(orderId)) {
			address = PackageItem.getAddress(box);
			if (box.has(AllDataComponents.PACKAGE_ORDER_DATA)) {
				PackageOrderWithCrafts context = box.get(AllDataComponents.PACKAGE_ORDER_DATA).orderContext();
				if (context != null && !context.isEmpty())
					orderContext = context;
			}

			ItemStackHandler contents = PackageItem.getContents(box);
			for (int slot = 0; slot < contents.getSlots(); slot++)
				summary.add(contents.getStackInSlot(slot));
		}

		List<BigItemStack> orderedStacks = new ArrayList<>();
		if (orderContext != null) {
			List<BigItemStack> packagesSplitByRecipe = repackBasedOnRecipes(summary, orderContext, address, r);
			exportingPackages.addAll(packagesSplitByRecipe);
			
			if (packagesSplitByRecipe.isEmpty())
				for (BigItemStack stack : orderContext.stacks())
					orderedStacks.add(new BigItemStack(stack.stack, stack.count));
		}

		List<BigItemStack> allItems = summary.getStacks();
		List<ItemStack> outputSlots = new ArrayList<>();

		Repack:
		while (true) {
			allItems.removeIf(e -> e.count == 0);
			if (allItems.isEmpty())
				break;

			BigItemStack targetedEntry = null;
			if (!orderedStacks.isEmpty())
				targetedEntry = orderedStacks.remove(0);

			ItemSearch:
			for (BigItemStack entry : allItems) {
				int targetAmount = entry.count;
				if (targetAmount == 0)
					continue;
				if (targetedEntry != null) {
					targetAmount = targetedEntry.count;
					if (!ItemStack.isSameItemSameComponents(entry.stack, targetedEntry.stack))
						continue;
				}

				while (targetAmount > 0) {
					int removedAmount = Math.min(Math.min(targetAmount, entry.stack.getMaxStackSize()), entry.count);
					if (removedAmount == 0)
						continue ItemSearch;

					ItemStack output = entry.stack.copyWithCount(removedAmount);
					targetAmount -= removedAmount;
					if (targetedEntry != null)
						targetedEntry.count = targetAmount;
					entry.count -= removedAmount;
					outputSlots.add(output);
				}

				continue Repack;
			}
		}

		int currentSlot = 0;
		ItemStackHandler target = new ItemStackHandler(PackageItem.SLOTS);

		for (ItemStack item : outputSlots) {
			target.setStackInSlot(currentSlot++, item);
			if (currentSlot < PackageItem.SLOTS)
				continue;
			exportingPackages.add(new BigItemStack(PackageItem.containing(target), 1));
			target = new ItemStackHandler(PackageItem.SLOTS);
			currentSlot = 0;
		}

		for (int slot = 0; slot < target.getSlots(); slot++)
			if (!target.getStackInSlot(slot)
				.isEmpty()) {
				exportingPackages.add(new BigItemStack(PackageItem.containing(target), 1));
				break;
			}

		for (BigItemStack box : exportingPackages)
			PackageItem.addAddress(box.stack, address);

		for (int i = 0; i < exportingPackages.size(); i++) {
			BigItemStack box = exportingPackages.get(i);
			boolean isfinal = i == exportingPackages.size() - 1;
			PackageOrderWithCrafts outboundOrderContext = isfinal && orderContext != null ? orderContext : null;
			if (PackageItem.getOrderId(box.stack) == -1)
				PackageItem.setOrder(box.stack, orderId, 0, true, 0, true, outboundOrderContext);
		}

		return exportingPackages;
	}

	private boolean isOrderComplete(int orderId) {
		boolean finalLinkReached = false;
		Links:
		for (int linkCounter = 0; linkCounter < 1000; linkCounter++) {
			if (finalLinkReached)
				break;
			Packages:
			for (int packageCounter = 0; packageCounter < 1000; packageCounter++) {
				for (ItemStack box : collectedPackages.get(orderId)) {
					PackageOrderData data = box.get(AllDataComponents.PACKAGE_ORDER_DATA);
					if (linkCounter != data.linkIndex())
						continue;
					if (packageCounter != data.fragmentIndex())
						continue;
					finalLinkReached = data.isFinalLink();
					if (data.isFinal())
						continue Links;
					continue Packages;
				}
				return false;
			}
		}
		return true;
	}

	protected List<BigItemStack> repackBasedOnRecipes(InventorySummary summary, PackageOrderWithCrafts order, String address, RandomSource r) {
		if (order.orderedCrafts().isEmpty())
			return List.of();
		
		List<BigItemStack> packages = new ArrayList<>();
		for (CraftingEntry craftingEntry : order.orderedCrafts()) {
			int packagesToCreate = 0;
			Crafts: for (int i = 0; i < craftingEntry.count(); i++) {
				for (BigItemStack required : craftingEntry.pattern().stacks()) {
					if (required.stack.isEmpty())
						continue;
					if (summary.getCountOf(required.stack) <= 0)
						break Crafts;
					summary.add(required.stack, -1);
				}
				packagesToCreate++;
			}
			
			ItemStackHandler target = new ItemStackHandler(PackageItem.SLOTS);
			List<BigItemStack> stacks = craftingEntry.pattern().stacks();
			for (int currentSlot = 0; currentSlot < Math.min(stacks.size(), target.getSlots()); currentSlot++)
				target.setStackInSlot(currentSlot, stacks.get(currentSlot).stack.copyWithCount(1));
			
			ItemStack box = PackageItem.containing(target);
			PackageItem.setOrder(box, r.nextInt(), 0, true, 0, true,
				PackageOrderWithCrafts.singleRecipe(craftingEntry.pattern()
					.stacks()));
			packages.add(new BigItemStack(box, packagesToCreate));
		}
		
		return packages;
	}

}
