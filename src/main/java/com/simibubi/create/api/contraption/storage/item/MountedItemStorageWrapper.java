package com.simibubi.create.api.contraption.storage.item;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

/**
 * Wrapper around many MountedItemStorages, providing access to all of them as one storage.
 * They can still be accessed individually through the map.
 */
public class MountedItemStorageWrapper extends CombinedInvWrapper {
	public final ImmutableMap<BlockPos, MountedItemStorage> storages;

	public MountedItemStorageWrapper(ImmutableMap<BlockPos, MountedItemStorage> storages) {
		super(storages.values().toArray(IItemHandlerModifiable[]::new));
		this.storages = storages;
	}
}
