package com.simibubi.create.foundation.utility;

import net.minecraft.nbt.CompoundTag;

public interface IPartialSafeNBT {
	/** This method always runs on the logical server. */
	public void writeSafe(CompoundTag compound);
}
