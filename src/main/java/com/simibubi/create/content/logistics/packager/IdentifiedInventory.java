package com.simibubi.create.content.logistics.packager;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.packager.InventoryIdentifier;

import net.neoforged.neoforge.items.IItemHandler;

/**
 * An item inventory, possibly with an associated InventoryIdentifier.
 */
public record IdentifiedInventory(@Nullable InventoryIdentifier identifier, IItemHandler handler) {
}
