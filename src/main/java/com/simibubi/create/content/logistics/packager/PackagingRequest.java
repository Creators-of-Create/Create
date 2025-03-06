package com.simibubi.create.content.logistics.packager;

import javax.annotation.Nullable;

import com.simibubi.create.content.logistics.stockTicker.PackageOrder;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderCraftingContext;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public record PackagingRequest(ItemStack item, MutableInt count, String address, int linkIndex,
							   MutableBoolean finalLink, MutableInt packageCounter, int orderId,
							   @Nullable PackageOrder context, @Nullable PackageOrderCraftingContext craftingContext) {

	public static PackagingRequest create(ItemStack item, int count, String address, int linkIndex,
										  MutableBoolean finalLink, int packageCount, int orderId, @Nullable PackageOrder context, @Nullable PackageOrderCraftingContext craftingContext) {
		return new PackagingRequest(item, new MutableInt(count), address, linkIndex, finalLink,
			new MutableInt(packageCount), orderId, context, craftingContext);
	}

	public int getCount() {
		return count.intValue();
	}

	public void subtract(int toSubtract) {
		count.setValue(getCount() - toSubtract);
	}

	public boolean isEmpty() {
		return getCount() == 0;
	}

	public static PackagingRequest fromNBT(CompoundTag tag) {
		ItemStack item = ItemStack.of(tag.getCompound("Item"));
		int count = tag.getInt("Count");
		String address = tag.getString("Address");
		int linkIndex = tag.getInt("LinkIndex");
		MutableBoolean finalLink = new MutableBoolean(tag.getBoolean("FinalLink"));
		int packageCount = tag.getInt("PackageCount");
		int orderId = tag.getInt("OrderId");
		PackageOrder context = tag.contains("OrderContext") ? PackageOrder.read(tag.getCompound("OrderContext")) : null;
		PackageOrderCraftingContext orderContext =
			tag.contains("OrderCraftingContext") ? PackageOrderCraftingContext.read(tag.getCompound("OrderCraftingContext")) : null;
		return create(item, count, address, linkIndex, finalLink, packageCount, orderId, context, orderContext);
	}

	public CompoundTag toNBT() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("Count", count.intValue());
		tag.put("Item", item.serializeNBT());
		tag.putString("Address", address);
		tag.putInt("LinkIndex", linkIndex);
		tag.putBoolean("FinalLink", finalLink.booleanValue());
		tag.putInt("PackageCount", packageCounter.intValue());
		tag.putInt("OrderId", orderId);
		if (context != null) {
			tag.put("OrderContext", context.write());
		}
		if (craftingContext != null) {
			tag.put("OrderCraftingContext", craftingContext.write());
		}
		return tag;
	}

}
