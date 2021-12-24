package com.simibubi.create.lib.extensions;

import com.simibubi.create.lib.mixin.accessor.BlockEntityAccessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockEntityExtensions {
	default CompoundTag create$getExtraCustomData() {
		return null;
	}

	void create$deserializeNBT(BlockState state, CompoundTag nbt);

	default CompoundTag create$save(CompoundTag tag) {
		((BlockEntityAccessor) this).callSaveMetadata(tag);
		return tag;
	};
}
