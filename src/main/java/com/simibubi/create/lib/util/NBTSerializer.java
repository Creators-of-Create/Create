package com.simibubi.create.lib.util;

import net.minecraft.nbt.CompoundTag;

public class NBTSerializer {
	public static void deserializeNBT(Object o, CompoundTag nbt) {
		((NBTSerializable) o).create$deserializeNBT(nbt);
	}

	public static CompoundTag serializeNBT(Object o) {
		return ((NBTSerializable) o).create$serializeNBT();
	}
}
