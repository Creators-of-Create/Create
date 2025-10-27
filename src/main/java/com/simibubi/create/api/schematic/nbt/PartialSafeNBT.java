package com.simibubi.create.api.schematic.nbt;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public interface PartialSafeNBT {
	/**
	 * This will always be called from the logical server
	 */
	void writeSafe(CompoundTag compound, HolderLookup.Provider registries);
}
