package com.simibubi.create.lib.block;

import net.minecraft.nbt.CompoundTag;

public interface CustomUpdateTagHandlingBlockEntity {
	void handleUpdateTag(CompoundTag tag);
}
