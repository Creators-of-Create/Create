package com.simibubi.create.lib.extensions;

import java.util.Collection;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;

public interface EntityExtensions {
	CompoundTag create$getExtraCustomData();

	Collection<ItemEntity> create$captureDrops();

	Collection<ItemEntity> create$captureDrops(Collection<ItemEntity> value);
}
