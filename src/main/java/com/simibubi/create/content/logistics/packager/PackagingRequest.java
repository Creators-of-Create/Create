package com.simibubi.create.content.logistics.packager;

import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public record PackagingRequest(ItemStack item, MutableInt count, String address, int linkIndex,
	MutableBoolean finalLink, MutableInt packageCounter, int orderId, @Nullable PackageOrderWithCrafts context) {

	public static PackagingRequest create(ItemStack item, int count, String address, int linkIndex,
		MutableBoolean finalLink, int packageCount, int orderId, @Nullable PackageOrderWithCrafts context) {
		return new PackagingRequest(item, new MutableInt(count), address, linkIndex, finalLink,
			new MutableInt(packageCount), orderId, context);
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
		PackageOrderWithCrafts context = tag.contains("OrderContext") ? PackageOrderWithCrafts.read(tag.getCompound("OrderContext")) : null;
		return create(item, count, address, linkIndex, finalLink, packageCount, orderId, context);
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
		if (context != null)
			tag.put("OrderContext", context.write());
		return tag;
	}

}
