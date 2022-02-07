package com.simibubi.create.lib.extensions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockEntityExtensions {
	default CompoundTag create$getExtraCustomData() {
		return null;
	}

	void create$deserializeNBT(BlockState state, CompoundTag nbt);
}
