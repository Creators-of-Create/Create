package com.simibubi.create.lib.extensions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockEntityExtensions {
	CompoundTag create$getExtraCustomData();

	void create$deserializeNBT(BlockState state, CompoundTag nbt);
}
