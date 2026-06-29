package com.simibubi.create.api.contraption.storage.item;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;

import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

/**
 * Wrapper around many MountedItemStorages, providing access to all of them as one storage.
 * They can still be accessed individually through the map.
 * 
 * Uses O(1) lookup arrays instead of O(n) linear scan.
 */
public class MountedItemStorageWrapper extends CombinedInvWrapper {
	public final ImmutableMap<BlockPos, MountedItemStorage> storages;
	
	// Lookup arrays
	private final int[] slotToStorage;   // Maps each slot to its storage index
	private final int[] slotOffsets;     // Starting slot for each storage

	public MountedItemStorageWrapper(ImmutableMap<BlockPos, MountedItemStorage> storages) {
		super(storages.values().toArray(IItemHandlerModifiable[]::new));
		this.storages = storages;
		
		// Build lookup arrays
		int totalSlots = getSlots();
		this.slotToStorage = new int[totalSlots];
		this.slotOffsets = new int[itemHandler.length];
		
		int currentSlot = 0;
		for (int storageIdx = 0; storageIdx < itemHandler.length; storageIdx++) {
			slotOffsets[storageIdx] = currentSlot;
			int slotsInStorage = itemHandler[storageIdx].getSlots();
			
			for (int i = 0; i < slotsInStorage; i++) {
				slotToStorage[currentSlot + i] = storageIdx;
			}
			
			currentSlot += slotsInStorage;
		}
	}
	
	@Override
	protected int getIndexForSlot(int slot) {
		if (slot < 0 || slot >= slotToStorage.length) {
			return -1;
		}
		return slotToStorage[slot];
	}
	
	@Override
	protected int getSlotFromIndex(int slot, int index) {
		return slot - slotOffsets[index];
	}
}
