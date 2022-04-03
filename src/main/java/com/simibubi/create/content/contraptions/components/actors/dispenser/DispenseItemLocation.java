package com.simibubi.create.content.contraptions.components.actors.dispenser;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;

public class DispenseItemLocation {
	private final boolean internal;
	private int slot;
	private ItemVariant variant;
	private int count;

	public static final DispenseItemLocation NONE = new DispenseItemLocation(-1);

	public DispenseItemLocation(int slot) {
		this.internal = true;
		this.slot = slot;
	}

	public DispenseItemLocation(ResourceAmount<ItemVariant> content) {
		this.internal = false;
		this.variant = content.resource();
		this.count = (int) Math.min(content.amount(), variant.getItem().getMaxStackSize());
	}

	public boolean isInternal() {
		return internal;
	}

	public int getSlot() {
		return slot;
	}

	public ItemVariant getVariant() {
		return variant;
	}

	public int getCount() {
		return count;
	}

	public boolean isEmpty() {
		return slot < 0;
	}
}
