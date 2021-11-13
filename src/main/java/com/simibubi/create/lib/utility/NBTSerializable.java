package com.simibubi.create.lib.utility;

import net.minecraft.nbt.CompoundTag;

public interface NBTSerializable {
	CompoundTag create$serializeNBT();

	void create$deserializeNBT(CompoundTag nbt);
}
